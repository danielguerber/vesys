package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bank.Account;
import bank.implementation.Bank;

@WebServlet("/deposit")
public class DepositServlet extends HttpServlet{
	
	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = 8439412655054799485L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String number = req.getParameter("number");
		if (number==null) number="";
		
		PrintWriter writer = resp.getWriter();
		resp.setContentType("text/html");	
		
		writer.write("<html><head><title>Bank</title></head><body><h1>Deposit Money</h1>");
		writer.write("<form action=\"deposit\" method=\"post\">");
		writer.write("Number:<br/> <input type=\"text\" name=\"number\" value=\"" + number + "\"readonly></input><br/>");
		writer.write("Amount:<br/> <input type=\"text\" name=\"amount\"/><br/>");
		writer.write("<input type=\"submit\" name=\"submit\" value=\"Deposit\"/>");
		writer.write("</form></body></html>");
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String error = null;
		String number = req.getParameter("number");
		String amount = req.getParameter("amount");
		
	    if (number == null || number == "") {
	    	error = "Number not provided!";
	    } else {
	    	if (amount == null || amount == "") {
	    		error="Amount not provided!";
	    	} else {
		    	try {
		    		Bank bank = Bank.getInstance();
					double dblAmount = Double.parseDouble(amount);
					Account a = bank.getAccount(number);
					if (a==null) {
						error="Account does not exist!";
					} else {
						a.deposit(dblAmount);
						resp.sendRedirect("");
					}
		    	}
		    	catch (NumberFormatException e) {
					error = "Illegal Format!";
				}
				catch (Exception e) {
					error = e.getMessage();
				}
	    	}
	    }
		
		
		PrintWriter writer = resp.getWriter();
		resp.setContentType("text/html");	
		
		writer.write("<html><head><title>Bank</title></head><body><h1>Error</h1>");
		if (error != null) {
			writer.write(error + "<br/>");
		}
		writer.write("<a href=\"deposit?number=");
		if (number!=null) {
			writer.write(number);
		}
		writer.write("\">Back</a>&nbsp;");
		writer.write("<a href=\"/bankservlet\">Home</a>");
		writer.write("</body></html>");
		
	}
}
