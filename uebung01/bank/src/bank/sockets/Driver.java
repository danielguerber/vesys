package bank.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import bank.InactiveException;
import bank.OverdrawException;

public class Driver implements bank.BankDriver {
	private Socket s;
	private PrintWriter out;
	private BufferedReader in;
	private Bank bank;
	
	@Override
	public void connect(String[] args) throws IOException {
		s = new Socket(args[0], Integer.parseInt(args[1]), null, 0);
		out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(),Charset.forName("UTF-16")));
		in = new BufferedReader(new InputStreamReader(s.getInputStream(),Charset.forName("UTF-16")));
		bank = new Bank(out, in);
	}

	@Override
	public void disconnect() throws IOException {
		bank = null;
		out.close();
		out = null;
		in.close();
		in = null;
		s.close();
		s = null;
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	static class Bank implements bank.Bank {
		private PrintWriter out;
		private BufferedReader in;
		
		public Bank(PrintWriter out, BufferedReader in) {
			this.out = out;
			this.in = in;
		}

		@Override
		public Set<String> getAccountNumbers() throws IOException {
			out.println("get-acc-numbers");
			out.flush();
			Set<String> accountNumbers = new HashSet<String>();
			String number = in.readLine();
			int count = Integer.parseInt(number);
			for (int i = 0; i < count; i++) {
				accountNumbers.add(in.readLine());
			}
			return accountNumbers;
		}

		@Override
		public String createAccount(String owner) throws IOException {
			out.println("create");
			out.println(owner);
			out.flush();
			if (in.readLine().equals("ok")) {
				return in.readLine();
			} else {
				return null;
			}
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			out.println("close");
			out.println(number);
			out.flush();
			if (in.readLine().equals("ok")) {
				return true;
			} else {
				return false;
			}
		}

		
		@Override
		public bank.Account getAccount(String number) throws IOException {
			out.println("get-acc");
			out.println(number);
			out.flush();
			if (in.readLine().equals("ok")) {
				return new Account(in.readLine(), out, in);
			} else {
				return null;
			}
			
		}

		/**
		 * Transfers the given amount from account a to account b.
		 * 
		 * @param a account to withdraw amount from
		 * @param b account to deposit amount
		 * @param amount value to transfer
		 * @pre amount >= 0
		 * @throws InactiveException if one of the two accounts is not active
		 * @throws OverdrawException if the amount is greater than the balance of
		 *             account a
		 * @throws IllegalArgumentException if the argument is negative
		 * @throws IOException if a remoting or communication problem occurs
		 */
		@Override
		public void transfer(bank.Account from, bank.Account to, double amount)
				throws IOException, InactiveException, OverdrawException {
			out.println("transfer");
			out.println(from.getNumber());
			out.println(to.getNumber());
			out.println(amount);
			out.flush();
			
			String status = in.readLine();
			if (!status.equals("ok")) {
				switch (status) {
					case "InactiveException":
						throw new InactiveException();
					case "OverdrawException":
						throw new OverdrawException();
					case "ArgumentException":
						throw new IllegalArgumentException();
					default:
						throw new IOException("Illegal status recieved!");
				}
			} 
		}

	}

	static class Account implements bank.Account {
		private String number;
		private PrintWriter out;
		private BufferedReader in;
		
		Account(String number, PrintWriter out, BufferedReader in) {
			this.number = number;
			this.out = out;
			this.in = in;
		}

		@Override
		public double getBalance() throws IOException {
			out.println("get-balance");
			out.println(number);
			out.flush();
			return Double.parseDouble(in.readLine());
		}

		@Override
		public String getOwner() throws IOException {
			out.println("get-owner");
			out.println(number);
			out.flush();
			return in.readLine();
		}

		@Override
		public String getNumber() {
			return number;
		}

		@Override
		public boolean isActive() throws IOException {
			out.println("get-active");
			out.println(number);
			out.flush();
			return in.readLine().equals("active");
		}

		@Override
		public void deposit(double amount) throws InactiveException, IllegalArgumentException, IOException {
			out.println("deposit");
			out.println(number);
			out.println(amount);
			out.flush();
			String status = in.readLine();
			if (!status.equals("ok")) {
				switch (status) {
					case "InactiveException":
						throw new InactiveException();
					case "ArgumentException":
						throw new IllegalArgumentException();
					default:
						throw new IOException("Illegal status recieved!");
				}
			} 
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException, IllegalArgumentException, IOException {
			out.println("withdraw");
			out.println(number);
			out.println(amount);
			out.flush();
			String status = in.readLine();
			if (!status.equals("ok")) {
				switch (status) {
					case "InactiveException":
						throw new InactiveException();
					case "ArgumentException":
						throw new IllegalArgumentException();
					case "OverdrawException":
						throw new OverdrawException();
					default:
						throw new IOException("Illegal status recieved!");
				}
			} 
		}

	}
}
