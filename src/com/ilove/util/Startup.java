package com.ilove.util;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import com.mongodb.Mongo;


// TO DO: do not map to URL
@WebServlet(name="Startup", value="/private/donotmap", loadOnStartup=1)

public class Startup extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Startup() {
        super();
        // TODO Auto-generated constructor stub
    }

	public void init(ServletConfig config) throws ServletException {
		System.out.println("WebApp log: Startup servlet init. Creating connection to Mongo");
		try {
			Mongo m = new Mongo( "localhost" , 27017 );
			config.getServletContext().setAttribute("mongo", m);
		} catch (Exception e) {
			System.out.println("WebApp error: "+e); 
		}
	}
	
	public void destroy(ServletConfig config) throws ServletException {
		System.out.println("WebApp log: Startup servlet destroyed. Closing connection to Mongo");
		Mongo m = (Mongo)(config.getServletContext().getAttribute("mongo"));
		if (m != null) {
			m.close(); 
		}
	}

}
