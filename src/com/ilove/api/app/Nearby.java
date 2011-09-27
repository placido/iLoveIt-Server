package com.ilove.api.app;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

// TO DO: ESCAPE JSON (quotes, etc...)

@WebServlet(description = "Main API method invoked by the app: returns nearby neighbourhoods and photos", urlPatterns = { "/api/app/nearby" })

public class Nearby extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public Nearby() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Mongo m = null;
		PrintWriter out = null;
		response.setContentType("application/json");
		
		try {
			out = response.getWriter();
			out.println("{");
			m = new Mongo( "localhost" , 27017 );
			DB db = m.getDB( "test" );
			
			// parse coordinates arguments
			double latitude = 51.48582; // default to VoHo!
			double longitude = -0.12205;
			try {
				latitude =  new Double(request.getParameter("lat"));
				longitude = new Double(request.getParameter("lng"));
			} catch (Exception argumentException) { }
			double location[] = { latitude, longitude };
			
			// parse paging arguments
			int limit = 10;
			int skip = 0;
			try {
				limit =  new Integer(request.getParameter("limit"));
				skip = new Integer(request.getParameter("skip"));
			} catch (Exception argumentException) { }
			if (limit > 100) limit = 100; // cap the limit in any case
			
			// parse user argument
			String user = null;
			if ((request.getParameter("user") != null) && (request.getParameter("user").matches("^\\w+$"))) {
				user = request.getParameter("user");
			}
			
			// Return nearby neighbourhoods
			DBCollection coll = db.getCollection("hoods");
			BasicDBObject query = new BasicDBObject("location" , new BasicDBObject( "$near", location));
			DBCursor cur = coll.find(query).limit(5); // return no more than 5 neighbourhoods
			if (cur.count() > 0) {
				out.println("\"hoods\":[");
		        while(cur.hasNext()) {
		            out.print(cur.next());
		            if (cur.hasNext()) out.println(", ");
		        }
				out.println("], ");
			}
		
			// Returns photos
			coll = db.getCollection("photos");
			if (user == null) {
				// look for paginated photos nearby in (default) distance order
				cur = coll.find(query).limit(limit).skip(skip);
			} else {
				// look for user photos in anti-chronological order
				query.append("user", user);
				cur = coll.find(query).sort(new BasicDBObject("ts", -1)).limit(limit).skip(skip);
			}
			if (cur.count() > 0) {
				out.println("\"photos\":[");
				while(cur.hasNext()) {
					out.print(cur.next());
					if (cur.hasNext()) out.println(", ");
				}
				out.println("]");
			}
			
		} catch (Exception e) {
			out.println("\"error\": \""+e+"\"");
			
		} finally {
			if (out != null) {
				out.println("}");
				out.close();
			}
			if (m != null) m.close();
		}
	}
}
