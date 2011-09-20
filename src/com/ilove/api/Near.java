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
@WebServlet("/api/near")
public class Near extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Near() {
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
		DBCollection coll = db.getCollection("hoods");
		double location[] = { new Double(request.getParameter("lat")), new Double(request.getParameter("lng"))};
        BasicDBObject query = new BasicDBObject( "location" , new BasicDBObject( "$near", location) );
		DBObject myDoc = coll.findOne(query);
		out.println(myDoc);
		out.close();	
	}
}
