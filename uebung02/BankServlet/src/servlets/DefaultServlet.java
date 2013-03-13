package servlets;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bank.Account;
import bank.implementation.Bank;

@WebServlet("/")
public class DefaultServlet extends HttpServlet {
	
	/**
	 * Generated Version UID
	 */
	private static final long serialVersionUID = -5601445569577136558L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		PrintWriter writer = response.getWriter();
		
		response.setContentType("text/html");
		
		Bank bank = Bank.getInstance();
		
		writer.write("<html><head><title>Bank</title></head><body><h1>Bank</h1>");
		writer.write("<a href=\"create\">Create Account</a><br/>");
		writer.write("<h2>Accounts:</h2>");
		writer.write("<table><tr><td><b>AccountNr</b></td><td><b>Owner</b></td><td><b>Balance</b></td><td></td></tr>");
		Set<String> accnumbers = bank.getAccountNumbers();
		for (String accnumber : accnumbers) {
			Account account = bank.getAccount(accnumber);
			writer.write("<tr><td>" + accnumber + "</td>");
			writer.write("<td>" + account.getOwner() + "</td>");
			writer.write(String.format("<td>%.2f</td>", account.getBalance()));
			writer.write("<td><a href=\"withdraw?number=" + accnumber + "\">Withdraw Money</a>&nbsp;");
			writer.write("<a href=\"deposit?number=" + accnumber + "\">Deposit Money</a>&nbsp;");
			if (accnumbers.size() > 1)
				writer.write("<a href=\"transfer?number=" + accnumber + "\">Transfer Money</a>&nbsp;");
			writer.write("<a href=\"close?number=" + accnumber + "\">Close Account</a></td>");
		}
		
		writer.write("</table>");
		writer.write("</body></html>");
	}
}
