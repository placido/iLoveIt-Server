package com.ilove.api;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import org.bson.types.ObjectId;

import com.ilove.aws.S3MyObject;
import com.ilove.util.ImageUtil;

import com.ilove.aws.S3StorageManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;


@WebServlet("/api/photo")

public class Photo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DB db = null;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Photo() {
        super();
    	try {
    		Mongo m = new Mongo( "localhost" , 27017 );
    		db = m.getDB( "test" );
    	} catch (Exception e) {
    		System.out.println("Failed to connect to database: "+e);
    	}
    }
    
    //Create a progress listener (OPTIONAL)
    ProgressListener progressListener = new ProgressListener() {
    private long kiloBytes = -1;
    public void update(long pBytesRead, long pContentLength, int pItems) {
    	long kBytes = pBytesRead / 1000;
        if (kiloBytes == kBytes) {
        	return;
        }
        kiloBytes = kBytes;
        if (pContentLength == -1) {
         //   System.out.println("So far, " + pBytesRead + " bytes of item "+pItems+" have been read.");
        } else {
         //   System.out.println("So far, " + pBytesRead + " of " + pContentLength + " bytes of item "+pItems+" have been read.");
        }
      }
    };


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Returns all photos
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		DBCollection coll = db.getCollection("photos");
		DBCursor cur = coll.find();
		out.println("{\"photos\":[");
        while(cur.hasNext()) {
            out.print(cur.next());
            if (cur.hasNext()) out.println(", ");
        }
		out.println("]}");
		out.close();
	}	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Upload a new photo
		long maxRequestSize = 2000000;
		int maxMemorySize = 2000000;
		String tmpDirPath = "C:\\tmp\\ilove\\tmp";
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		// Check that we have a file upload request
		// boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory(maxMemorySize, new File(tmpDirPath));
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		// Set overall request size constraint
		upload.setSizeMax(maxRequestSize);
		// BInd the progress listener (optional)
		upload.setProgressListener(progressListener);
		
		// Insert record in DB and get the ID
		Date now = new Date();
		BasicDBObject time = new BasicDBObject("ts", now);
		DBCollection coll = db.getCollection("photos");
		coll.insert(time);
		ObjectId id = (ObjectId)time.get( "_id" );
		String dbId = id.toString();

		
		// Parse the request
		try {
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
			        out.println("<img src=\""+obj.getAwsUrl()+"\"><br>");
			        
			        // Crop
			        byte [] cropped = ImageUtil.cropPhoto(photoData);
			        
			        // Resize small and store on S3
			        byte [] small = ImageUtil.scalePhoto(160, cropped);
			        obj = new S3MyObject(small, "neighbourhoods", "uploads/"+dbId+"/small.jpeg", item.getContentType());
			        mgr.storePublicRead(obj, false);
			        out.println("<img src=\""+obj.getAwsUrl()+"\"><br>");
			        
			        // Resize large and store on S3
			        byte [] large = ImageUtil.scalePhoto(320, cropped);
			        obj = new S3MyObject(large, "neighbourhoods", "uploads/"+dbId+"/large.jpeg", item.getContentType());
			        mgr.storePublicRead(obj, false);
			        out.println("<img src=\""+obj.getAwsUrl()+"\"><br>");

			    }
			}
			
		    out.println("<em>"+caption+"</em><br>Picture taken at ("+location[0]+", "+location[1]+") by user ID "+userId+"<p><a href=\"../photos.html\">See photos</a></html>");
			
		    // update the record
		    BasicDBObject docToUpdate = new BasicDBObject();
	        docToUpdate.put("_id", new org.bson.types.ObjectId(dbId));
	        BasicDBObject docUpdate = new BasicDBObject();
	        docUpdate.put("caption", caption);
	        docUpdate.put("user", userId);
	        docUpdate.put("location", location);
			coll.update(docToUpdate, docUpdate);
		    

		} catch (FileUploadException e) {
			// TO DO: handle file size exception
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
