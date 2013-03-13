package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bank.implementation.Bank;

@WebServlet("/close")
public class CloseServlet extends HttpServlet{
	
	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = -3483736040819510349L;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String number = req.getParameter("number");
		if (number==null) number="";
		
		PrintWriter writer = resp.getWriter();
		resp.setContentType("text/html");	
		
		writer.write("<html><head><title>Bank</title></head><body><h1>Close Account</h1>");
		writer.write("<form action=\"close\" method=\"post\">");
		writer.write("Number:<br/> <input type=\"text\" name=\"number\" value=\"" + number + "\"readonly></input><br/>");
		writer.write("Close this Account?<br/>");
		writer.write("<input type=\"submit\" name=\"submit\" value=\"OK\"/>");
		writer.write("</form></body></html>");
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String error = null;
		String number = req.getParameter("number");
		
	    if (number == null || number == "") {
	    	error = "Number not provided!";
	    } else {
	    	try {
	    		Bank bank = Bank.getInstance();
				if(bank.closeAccount(number)) {
					resp.sendRedirect("");
				} else {
					error = "Could not close Account!";
				}
	    	}
			catch (Exception e) {
				error = e.getMessage();
			}
	    }
		
		
		PrintWriter writer = resp.getWriter();
		resp.setContentType("text/html");	
		
		writer.write("<html><head><title>Bank</title></head><body><h1>Error</h1>");
		if (error != null) {
			writer.write(error + "<br/>");
		}
		writer.write("<a href=\"withdraw?number=");
		if (number!=null) {
			writer.write(number);
		}
		writer.write("\">Back</a>&nbsp;");
		writer.write("<a href=\"/bankservlet\">Home</a>");
		writer.write("</body></html>");
		
	}
}
