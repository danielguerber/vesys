package bank.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import bank.InactiveException;
import bank.OverdrawException;

public class Driver implements bank.BankDriver {
	private Bank bank = null;

	@Override
	public void connect(String[] args) {
		bank = new Bank();
		System.out.println("connected...");
	}

	@Override
	public void disconnect() {
		bank = null;
		System.out.println("disconnected...");
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	static class Bank implements bank.Bank {

		private Map<String, Account> accounts = new HashMap<String, Account>();

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
		public String createAccount(String owner) {
			Account newAccount = new Account(owner);
			accounts.put(newAccount.number, newAccount);
			return newAccount.number;
		}

		@Override
		public boolean closeAccount(String number) {
			Account closeAccount = accounts.get(number);
			if (closeAccount != null 
					&& closeAccount.getBalance() == 0
					&& closeAccount.isActive()) {
				closeAccount.active = false;
				return true;
			} else {
				return false;
			}
		}

		
		@Override
		public bank.Account getAccount(String number) {
			return (bank.Account) accounts.get(number);
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

	static class Account implements bank.Account {
		private String number;
		private String owner;
		private double balance;
		private boolean active = true;

		Account(String owner) {
			this.owner = owner;
			this.number = UUID.randomUUID().toString();
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
		public void deposit(double amount) throws InactiveException, IllegalArgumentException {
			if (!this.active) {
				throw new InactiveException("Account is closed!");
			}
			
			if (amount < 0) {
				throw new IllegalArgumentException("Amount can't be negative!");
			}
			
			this.balance += amount;
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException, IllegalArgumentException {
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

	}

}