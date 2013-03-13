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

@WebServlet("/create")
public class CreateServlet extends HttpServlet{
	
	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = 8439412655054799485L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		PrintWriter writer = resp.getWriter();
		resp.setContentType("text/html");	
		
		writer.write("<html><head><title>Bank</title></head><body><h1>Create Account</h1>");
		writer.write("<form action=\"create\" method=\"post\">");
		writer.write("Owner:<br/> <input type=\"text\" name=\"owner\"/><br/>");
		writer.write("Balance:<br/> <input type=\"text\" name=\"balance\"/><br/>");
		writer.write("<input type=\"submit\" name=\"submit\" value=\"Create\"/>");
		writer.write("</form></body></html>");
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String owner = req.getParameter("owner");
		String balance = req.getParameter("balance");
		
		String error = null;
		
		if (owner==null || owner=="") {
			error="Owner not set!";
		} else {
			Bank bank = Bank.getInstance();
			String number = bank.createAccount(owner);
			
			if(number==null){
				error = "Account could not be created";
			}
			else {
				try {
					Account acc = bank.getAccount(number);
					double amount;
					if( balance==null || balance.equals("")) amount=0;
					else amount = Double.parseDouble(balance);
					acc.deposit(amount);
					resp.sendRedirect("");
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
		writer.write("<a href=\"create\">Back</a>");
		writer.write("</body></html>");
		
	}
}
