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
import bank.sockets.Driver.NetworkHandler;

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
		NetworkHandler handler = new NetworkHandler(out, in);
		bank = new Bank(handler);
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
	
	static class NetworkHandler {
		private PrintWriter out;
		private BufferedReader in;
		
		public NetworkHandler(PrintWriter out, BufferedReader in) {
			this.out = out;
			this.in = in;
		}
		
		public String[] sendMessage(String command, String... args) throws IOException {
			StringBuilder sb = new StringBuilder(escape(command));
			for (int i = 0; i < args.length; i++) {
				sb.append(":");
				sb.append(escape(args[i]));
			}
			out.println(sb.toString());
			out.flush();
			
			String[] message = in.readLine().split(":");
			for (int i = 0; i < message.length; i++) {
				message[i] = unescape(message[i]);
			}
			
			if (message.length == 0 || message[0] == null) {
				throw new IOException("Illegal message recieved!");
			}
			
			return message;
		}
		
		private static String escape(String s) {
			return s.replace("\n","").replace(":", "[colon]");
		}
		
		private static String unescape(String s) {
			return s.replace("\n","").replace("[colon]", ":");
		}
	}

	static class Bank implements bank.Bank {
		private NetworkHandler handler;

		public Bank(NetworkHandler handler) {
			this.handler = handler;
		}

		@Override
		public Set<String> getAccountNumbers() throws IOException {
			String[] message = handler.sendMessage("get-acc-numbers");
			
			Set<String> accountNumbers = new HashSet<String>();
			int count = Integer.parseInt(message[0]);
			if (message.length < count + 1) {
				throw new IOException("Invalid message!");
			}
			
			for (int i = 1; i <= count; i++) {
				accountNumbers.add(message[i]);
			}
			return accountNumbers;
		}

		@Override
		public String createAccount(String owner) throws IOException {
			String[] message = handler.sendMessage("create", owner);
			if (message[0].equals("ok") && message.length > 1) {
				return message[1];
			} else {
				return null;
			}
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			String[] message = handler.sendMessage("close", number);
			if (message[0].equals("ok")) {
				return true;
			} else {
				return false;
			}
		}

		
		@Override
		public bank.Account getAccount(String number) throws IOException {
			String[] message = handler.sendMessage("get-acc", number);
			if (message[0].equals("ok") && message.length > 1) {
				return new Account(message[1], handler);
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
			String[] message = handler.sendMessage("transfer", from.getNumber(), to.getNumber(), String.valueOf(amount));
			
			String status = message[0];
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
		private NetworkHandler handler;
		
		Account(String number, NetworkHandler handler) {
			this.number = number;
			this.handler = handler;
		}

		@Override
		public double getBalance() throws IOException {
			String[] message = handler.sendMessage("get-balance", number);
			return Double.parseDouble(message[0]);
		}

		@Override
		public String getOwner() throws IOException {
			String[] message = handler.sendMessage("get-owner", number);
			return message[0];
		}

		@Override
		public String getNumber() {
			return number;
		}

		@Override
		public boolean isActive() throws IOException {
			String[] message = handler.sendMessage("get-active", number);
			return message[0].equals("active");
		}

		@Override
		public void deposit(double amount) throws InactiveException, IllegalArgumentException, IOException {
			String[] message = handler.sendMessage("deposit", number, String.valueOf(amount));
			String status = message[0];
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
			String[] message = handler.sendMessage("withdraw", number, String.valueOf(amount));
			String status = message[0];
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
