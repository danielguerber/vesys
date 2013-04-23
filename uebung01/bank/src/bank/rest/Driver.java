package bank.rest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;

import bank.InactiveException;
import bank.OverdrawException;
import bank.types.AccountData;
import bank.types.AccountURLs;
import bank.types.TransactionData;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Driver implements bank.BankDriver {
	private String url;
	private Bank bank;
	
	@Override
	public void connect(String[] args) throws IOException {
		url = args[0];
		bank = new Bank(url);
	}

	@Override
	public void disconnect() throws IOException {
		bank = null;
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	static class Bank implements bank.Bank {
		private String url;
		Client c;
		WebResource r;
		WebResource rTransfer;
		private Set<String> accountNumbers;
		private EntityTag modified;

		public Bank(String url) {
			this.url = url;
			c = Client.create();
			r = c.resource(url + "accounts/");
			rTransfer = c.resource(url + "transfer/");
		}
		
		@Override
		public Set<String> getAccountNumbers() throws IOException {
			
			ClientResponse response;
			if (accountNumbers==null) {
				response = r.get(ClientResponse.class);
			} else {
				response = r.header("If-None-Match", modified).get(ClientResponse.class);
			}
			
			
			switch(response.getClientResponseStatus()) {
				case OK:
					accountNumbers = new HashSet<String>();
					for (String url : response.getEntity(AccountURLs.class).getUrl()) {
						accountNumbers.add(url.substring(url.lastIndexOf('/')+1));
					}
					modified = response.getEntityTag();
					return accountNumbers;
				case NOT_MODIFIED:
					return accountNumbers;
				default:
					throw new IOException("Error connecting to the server");
			} 
			
		}

		@Override
		public String createAccount(String owner) throws IOException {
			ClientResponse response = r.type(MediaType.TEXT_PLAIN).post(ClientResponse.class, owner);
			if (response.getClientResponseStatus() == ClientResponse.Status.CREATED) {
				String accUrl = response.getLocation().toString();
				return accUrl.substring(accUrl.lastIndexOf('/')+1);
			} else {
				throw new IOException(response.getClientResponseStatus().getReasonPhrase());
			}
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			WebResource r = c.resource(url + "accounts/" + number);
			ClientResponse response = r.delete(ClientResponse.class);
			switch (response.getClientResponseStatus()) {
				case OK:
				case NO_CONTENT:
					return true;
				case CONFLICT:
					return false;
				default:
					throw new IOException(response.getClientResponseStatus().getReasonPhrase());
			}
		}

		
		@Override
		public bank.Account getAccount(String number) throws IOException {
			if (number.length() > 0) {
				WebResource r = c.resource(url + "accounts/" + number);
				ClientResponse response = r.get(ClientResponse.class);
				switch (response.getClientResponseStatus()) {
					case OK:
						return new Account(response.getEntity(AccountData.class), r);
					case NOT_FOUND:
						return null;
					default:
						throw new IOException("Error connecting to the server");
				}
			} else {
				return null;
			}
		}
		
		@Override
		public void transfer(bank.Account fromAcc, bank.Account toAcc, double amount)
				throws IOException, InactiveException, OverdrawException {
			Account from = (Account)fromAcc;
			Account to = (Account)toAcc;
			
			boolean pending = true;
			
			if (amount < 0) {
				throw new IllegalArgumentException();
			}
			
			while (pending) {
				from.update();
				to.update();
				
				if (!from.isActive() || !to.isActive()) {
					throw new InactiveException();
				}
				
				if (amount > from.getBalance()) {
					throw new OverdrawException();
				}
				
				TransactionData data = new TransactionData();
				data.setFromETag(from.modified);
				data.setFromAmount(from.balance - amount);
				data.setFromNumber(from.number);
				data.setToEtag(to.modified);
				data.setToAmount(to.balance + amount);
				data.setToNumber(to.number);
				
				ClientResponse response = rTransfer.post(ClientResponse.class, data);
				
				switch(response.getClientResponseStatus()) {
					case NO_CONTENT:
						pending=false;
						break;
					case CONFLICT:
						pending=true;
						break;
					default:
						throw new IOException(response.getClientResponseStatus().getReasonPhrase());
				}
			}
		}
	}
	
	static class Account implements bank.Account {
		private final String number;
		private final String owner;
		private volatile double balance;
		private volatile boolean active = true;
		private volatile String modified;
		private WebResource r;

		public Account(AccountData entity, WebResource r) {
			this.number = entity.getNumber();
			this.owner = entity.getOwner();
			this.balance = entity.getBalance();
			this.active = entity.isActive();
			this.modified = String.valueOf(entity.getModified());
			this.r = r;
		}
		
		public synchronized void update() throws IOException {
			ClientResponse response = r.get(ClientResponse.class);
			switch (response.getClientResponseStatus()) {
				case OK:
					AccountData acc = response.getEntity(AccountData.class);
					this.active = acc.isActive();
					this.balance = acc.getBalance();
					this.modified = String.valueOf(acc.getModified());
					break;
				default:
					throw new IOException(response.getClientResponseStatus().getReasonPhrase());
			}
		}
		
		public String getModified() {
			return modified;
		}

		@Override
		public double getBalance() throws IOException {
			update();
			return balance;
		}

		@Override
		public String getOwner() {
			return owner;
		}

		@Override
		public String getNumber() {
			return number;
		}

		@Override
		public boolean isActive() throws IOException {
			update();
			return active;
		}

		@Override
		public synchronized void deposit(double amount) throws InactiveException, IllegalArgumentException, IOException {
			boolean commited = false;
			while (!commited) {
				update();
				
				if (!this.active) {
					throw new InactiveException("Account is closed!");
				}
				
				if (amount < 0) {
					throw new IllegalArgumentException("Amount can't be negative!");
				}
				
				ClientResponse response = r.header("If-Match", new EntityTag(modified)).put(ClientResponse.class,String.valueOf(balance+amount));
				
				switch(response.getClientResponseStatus()) {
					case NO_CONTENT:
						commited=true;
						break;
					case PRECONDITION_FAILED:
						commited=false;
						break;
					default:
						throw new IOException(response.getClientResponseStatus().getReasonPhrase());
				}
			}
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException, IllegalArgumentException, IOException {
			boolean commited = false;
			while (!commited) {
				update();
				
				if (!this.active) {
					throw new InactiveException("Account is closed!");
				}
				
				if (amount < 0) {
					throw new IllegalArgumentException("Amount can't be negative!");
				}
				
				if (amount > balance) {
					throw new OverdrawException("Not enough money on account!");
				}
				
				ClientResponse response = r.header("If-Match", new EntityTag(modified)).put(ClientResponse.class,String.valueOf(balance-amount));
				
				switch(response.getClientResponseStatus()) {
					case NO_CONTENT:
						commited=true;
						break;
					case PRECONDITION_FAILED:
						commited=false;
						break;
					default:
						throw new IOException(response.getClientResponseStatus().getReasonPhrase());
				}
			}
		}

	}
	
}
