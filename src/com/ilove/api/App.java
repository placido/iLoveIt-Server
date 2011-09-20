package com.ilove.api;

import java.io.IOException;
import java.io.PrintWriter;
import com.mongodb.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Near
 */
@WebServlet("/api/app")
public class App extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public App() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		Mongo m = new Mongo( "localhost" , 27017 );
		DB db = m.getDB( "test" );
		double location[] = { new Double(request.getParameter("lat")), new Double(request.getParameter("lng"))};
		
		// Return nearby neighbourhoods
		DBCollection coll = db.getCollection("hoods");
		BasicDBObject query = new BasicDBObject( "location" , new BasicDBObject( "$near", location));
		DBCursor cur = coll.find(query);
		out.println("{\"hoods\":[");
        while(cur.hasNext()) {
            out.print(cur.next());
            if (cur.hasNext()) out.println(", ");
        }
		out.println("], ");

		// Returns nearby photos
		coll = db.getCollection("photos");
		cur = coll.find(query).limit(8);
		out.println("\"photos\":[");
        while(cur.hasNext()) {
            out.print(cur.next());
            if (cur.hasNext()) out.println(", ");
        }
		out.println("]}");
		out.close();
		m.close();
	}
}
