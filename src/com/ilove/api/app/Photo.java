package com.ilove.api.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bson.types.ObjectId;

import com.ilove.aws.S3MyObject;
import com.ilove.aws.S3StorageManager;
import com.ilove.util.ImageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

@WebServlet("/api/app/photo")
public class Photo extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
    public Photo() {
        super();
    }

	// POST: upload a new photo
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long maxRequestSize = 5000000;
		int maxMemorySize = 5000000;
		// TO DO: Set correct max size and tmp folder
		String tmpDirPath = "C:\\tmp\\ilove\\tmp";
		PrintWriter out = null;
		Mongo m = null;
		response.setContentType("application/json");

		try {
			out = response.getWriter();
		
			// Insert record in DB with timestamp and get the ID
			m = new Mongo( "localhost" , 27017 );
			DB db = m.getDB( "test" );
			Date now = new Date();
			BasicDBObject time = new BasicDBObject("ts", now);
			DBCollection coll = db.getCollection("photos");
			coll.insert(time);
			ObjectId id = (ObjectId)time.get( "_id" );
			String dbId = id.toString();

			// Parse the request
			DiskFileItemFactory factory = new DiskFileItemFactory(maxMemorySize, new File(tmpDirPath));
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setSizeMax(maxRequestSize);
			List items = upload.parseRequest(request);
			// Process the uploaded items
			Iterator iter = items.iterator();
			
		    double[] location = new double[2];
		    String caption = null; 
		    String userId = null;
		    		
			while (iter.hasNext()) {
			    FileItem item = (FileItem) iter.next();
			    if (item.isFormField()) {
			    	// Standard form element
			        // out.println("Standard form element "+item.getFieldName()+"="+item.getString());	
			        if (item.getFieldName().equals("lat")) {
			        	location[0] = Double.parseDouble(item.getString());
			        }
			        if (item.getFieldName().equals("lng")) {
			        	location[1] = Double.parseDouble(item.getString());
			        }
			        if (item.getFieldName().equals("caption")) {
			        	caption = item.getString();
			        }
			        if (item.getFieldName().equals("user")) {
			        	userId = item.getString();
			        }
			    } else {
			    	// Upload form element
			        // out.println("Upload form element "+item.getName()+" ("+item.getSize()+" bytes, "+item.getContentType()+")");
			        InputStream uploadedStream = item.getInputStream();
			        byte [] photoData = new byte[(int)item.getSize()];
			        uploadedStream.read(photoData, 0, (int)item.getSize());
			        uploadedStream.close();

			        // Store on S3
			        S3MyObject obj = new S3MyObject(photoData, "neighbourhoods", "uploads/"+dbId+"/original.jpeg", item.getContentType());
			        S3StorageManager mgr = new S3StorageManager();
			        mgr.storePublicRead(obj, false);
			        
			        // Crop
			        byte [] cropped = ImageUtil.cropPhoto(photoData);
			        
			        // Resize small and store on S3
			        byte [] small = ImageUtil.scalePhoto(160, cropped);
			        obj = new S3MyObject(small, "neighbourhoods", "uploads/"+dbId+"/small.jpeg", item.getContentType());
			        mgr.storePublicRead(obj, false);
			        
			        // Resize large and store on S3
			        byte [] large = ImageUtil.scalePhoto(320, cropped);
			        obj = new S3MyObject(large, "neighbourhoods", "uploads/"+dbId+"/large.jpeg", item.getContentType());
			        mgr.storePublicRead(obj, false);
			    }
			}
						
		    // update the record
		    BasicDBObject docToUpdate = new BasicDBObject();
	        docToUpdate.put("_id", new org.bson.types.ObjectId(dbId));
	        BasicDBObject docUpdate = new BasicDBObject();
	        docUpdate.put("_id", new org.bson.types.ObjectId(dbId));
	        docUpdate.put("ts", now);
	        docUpdate.put("caption", caption);
	        docUpdate.put("user", userId);
	        docUpdate.put("location", location);
			coll.update(docToUpdate, docUpdate);
		    out.println(docUpdate.toString());

		} catch (Exception e) {
			// TO DO: remove from database the photos record placeholder with timestamp
			// TO DO: remove from S3
			out.println("{ \"error\": \""+e+"\" }");
			
		} finally {
			if (out != null) {
				out.close();
			}
			if (m != null) m.close();
		}
	}

}
