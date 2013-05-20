package bank.rmi;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import bank.BankDriver2.UpdateHandler;
import bank.InactiveException;
import bank.OverdrawException;

public class Server {
	
	public static void main(String args[]) throws Exception {
		try {
			LocateRegistry.createRegistry(1099);
		}
		catch (RemoteException e) {
			System.out.println(">> registry could not be exported");
			System.out.println(">> probably another registry already runs on 1099");
		}
		
		RemoteBank bank = new Bank();
		Naming.rebind("bankService", bank);
	}
	
	@SuppressWarnings("serial")
	static class Bank extends UnicastRemoteObject implements RemoteBank {

		private final Map<String, Account> accounts = new ConcurrentHashMap<String, Account>();
		private final List<RemoteUpdateHandler> updateHandlers = new ArrayList<RemoteUpdateHandler>();
		
		public Bank() throws RemoteException {
		}

		@Override
		public void registerUpdateHandler(RemoteUpdateHandler handler) throws IOException {
			updateHandlers.add(handler);
		}
		
		@Override
		public Set<String> getAccountNumbers() {
			Set<String> activeNumbers = new HashSet<String>();
			for (Account account : accounts.values()) {
				if (account.isActive()) {
					activeNumbers.add(account.getNumber());
				}
			}
			return activeNumbers;
		}

		@Override
		public String createAccount(String owner) throws IOException {
			Account newAccount = new Account(owner, updateHandlers);
			accounts.put(newAccount.number, newAccount);
			notifyHandlers(newAccount.number);
			return newAccount.number;
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			boolean ret=false;
			Account closeAccount = accounts.get(number);
			if (closeAccount != null ) {
				synchronized (closeAccount) {
					if (closeAccount.getBalance() == 0
							&& closeAccount.isActive()) {
						closeAccount.active = false;
						ret = true;
					}
				}
					
				if (ret) {
					notifyHandlers(closeAccount.number);
				}
			} 
			return ret;
		}

		
		@Override
		public Account getAccount(String number) {
			return accounts.get(number);
		}

		@Override
		public void transfer(bank.Account from, bank.Account to, double amount)
				throws IOException, InactiveException, OverdrawException {
			
			bank.Account first, second; 
			if (from.getNumber().compareTo(to.getNumber())<0) { 
				first = from; 
				second = to; 
			} else { 
				first = to; 
				second = from; 
			} 
			
			synchronized(first) {
				synchronized(second) {
					if (!from.isActive()) {
						throw new InactiveException("Source account is closed!");
					}
					
					if (!to.isActive()) {
						throw new InactiveException("Target account is closed!");
					}
					
					from.withdraw(amount);
					to.deposit(amount);
				}
			}
		}

		private void notifyHandlers(String id) throws IOException {
			for (UpdateHandler handler : updateHandlers) {
				handler.accountChanged(id);
			}
		}
	}

	@SuppressWarnings("serial")
	static class Account extends UnicastRemoteObject implements RemoteAccount {
		private final String number;
		private final String owner;
		private volatile double balance;
		private volatile boolean active = true;
		private final List<RemoteUpdateHandler> updateHandlers;

		Account(String owner, List<RemoteUpdateHandler> updateHandlers) throws IOException {
			this.owner = owner;
			this.number = UUID.randomUUID().toString();
			this.updateHandlers = updateHandlers;
		}

		@Override
		public double getBalance() {
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
		public boolean isActive() {
			return active;
		}

		@Override
		public void deposit(double amount) throws InactiveException, IllegalArgumentException, IOException {
			synchronized(this) {
				if (!this.active) {
					throw new InactiveException("Account is closed!");
				}
				
				if (amount < 0) {
					throw new IllegalArgumentException("Amount can't be negative!");
				}
				this.balance += amount;
			}
			notifyHandlers();
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException, IllegalArgumentException, IOException {
			synchronized(this) {
				if (!this.active) {
					throw new InactiveException("Account is closed!");
				}
				
				if (amount < 0) {
					throw new IllegalArgumentException("Amount can't be negative!");
				}
				
				if (amount > balance) {
					throw new OverdrawException("Not enough money on account!");
				}
				
				this.balance -= amount;
			}
			notifyHandlers();
		}
		
		private void notifyHandlers() throws IOException {
			for (UpdateHandler handler : updateHandlers) {
				handler.accountChanged(this.number);
			}
		}

	}
}
