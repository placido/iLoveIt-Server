package com.ilove.api.cms;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.mongodb.*;

/**
 * Servlet implementation class Marker
 */
@WebServlet("/api/cms/marker")

public class Marker extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DB db = null;
	
    /**
     * Constructor: connect to the database
     */
    public Marker() {
    	try {
    		Mongo m = new Mongo( "localhost" , 27017 );
    		db = m.getDB( "test" );
    	} catch (Exception e) {
    		System.out.println("Failed to connect to database: "+e);
    	}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// returns all markers 
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		DBCollection coll = db.getCollection("hoods");
		DBCursor cur = coll.find();
		out.println("{\"neighbourhoods\":[");
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
		// create a new marker
		response.setContentType("application/json");
		try {
			String name = request.getParameter("name");
			double location[] = {new Double(request.getParameter("lat")), new Double(request.getParameter("lng"))};
			BasicDBObject doc = new BasicDBObject();
	        doc.put("name", name);
	        doc.put("location", location);
			DBCollection coll = db.getCollection("hoods");
			coll.insert(doc);
			PrintWriter out = response.getWriter();
			out.println(doc);	
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	
	/**
	 * @see HttpServlet#doDelete(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// delete a marker
		response.setContentType("application/json");
		try {
			String dbId = request.getParameter("dbId");
			if (dbId != null) {
				BasicDBObject doc = new BasicDBObject();
		        doc.put("_id", new org.bson.types.ObjectId(dbId));
				DBCollection coll = db.getCollection("hoods");
				coll.remove(doc);
			}
		
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// update a marker
		response.setContentType("application/json");
		try {
			String dbId = request.getParameter("dbId");
			String name = request.getParameter("name");
			double location[] = {new Double(request.getParameter("lat")), new Double(request.getParameter("lng"))};
			BasicDBObject docToUpdate = new BasicDBObject();
	        docToUpdate.put("_id", new org.bson.types.ObjectId(dbId));
			BasicDBObject docUpdate = new BasicDBObject();
	        docUpdate.put("location", location);
	        docUpdate.put("name", name);
			DBCollection coll = db.getCollection("hoods");
			coll.update(docToUpdate, docUpdate);
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
