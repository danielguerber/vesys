using System;
using System.Collections.Generic;
using System.Linq;

namespace BankServer
{
    class Bank
    {
        private readonly Dictionary<string, Account> _accounts = new Dictionary<string, Account>();

		public IEnumerable<string> GetAccountNumbers() {
			return _accounts.Values.Where(account => account.IsActive).Select(element => element.Number);
		}

		public string CreateAccount(string owner) {
			var newAccount = new Account(owner);
			_accounts.Add(newAccount.Number, newAccount);
			return newAccount.Number;
		}

		public bool CloseAccount(string number)
		{
		    Account closeAccount;
		    if (!_accounts.TryGetValue(number, out closeAccount) || !closeAccount.Balance.Equals(0) || !closeAccount.IsActive)
		    {
		        return false;
		    }
		    closeAccount.IsActive = false;
		    return true;
		}


        public Account GetAccount(String number)
		{
		    Account account;
		    _accounts.TryGetValue(number, out account);
		    return account;
		}

		
		public void Transfer(Account from, Account to, double amount) {
			
			if (!from.IsActive || !to.IsActive) {
				throw new InactiveException();
			}
			
			from.Withdraw(amount);
			to.Deposit(amount);
		}

	
    }
}
