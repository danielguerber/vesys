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

@WebServlet("/transfer")
public class TransferServlet extends HttpServlet{
	
	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = 1837264773636513445L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String number = req.getParameter("number");
		if (number==null) number="";
		
		PrintWriter writer = resp.getWriter();
		resp.setContentType("text/html");	
		
		writer.write("<html><head><title>Bank</title></head><body><h1>Transfer Money</h1>");
		writer.write("<form action=\"transfer\" method=\"post\">");
		writer.write("From:<br/> <input type=\"text\" name=\"number\" value=\"" + number + "\"readonly></input><br/>");
		writer.write("To:<br/> <select name=\"to\">");
		for (String accnumber : Bank.getInstance().getAccountNumbers()) {
			if (!accnumber.equals(number))
				writer.write("<option value=\"" + accnumber + "\">" + accnumber + "</option>");
		}
		writer.write("</select><br/>");
		writer.write("Amount:<br/> <input type=\"text\" name=\"amount\"/><br/>");
		writer.write("<input type=\"submit\" name=\"submit\" value=\"Transfer\"/>");
		writer.write("</form></body></html>");
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String error = null;
		String number = req.getParameter("number");
		String to = req.getParameter("to");
		String amount = req.getParameter("amount");
		
	    if (number == null || number == "") {
	    	error = "Number not provided!";
	    } else {
	    	if (amount == null || amount == "") {
	    		error="Amount not provided!";
	    	} else {
	    		if (to == null || to == "") {
		    		error="To not provided!";
		    	} else {
			    	try {
			    		Bank bank = Bank.getInstance();
						double dblAmount = Double.parseDouble(amount);
						Account a = bank.getAccount(number);
						Account b = bank.getAccount(to);
						if (a==null || b==null) {
							error="Account does not exist!";
						} else {
							bank.transfer(a,b,dblAmount);
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
	    }
		
		
		PrintWriter writer = resp.getWriter();
		resp.setContentType("text/html");	
		
		writer.write("<html><head><title>Bank</title></head><body><h1>Error</h1>");
		if (error != null) {
			writer.write(error + "<br/>");
		}
		writer.write("<a href=\"transfer?number=");
		if (number!=null) {
			writer.write(number);
		}
		writer.write("\">Back</a>&nbsp;");
		writer.write("<a href=\"/bankservlet\">Home</a>");
		writer.write("</body></html>");
		
	}
}
