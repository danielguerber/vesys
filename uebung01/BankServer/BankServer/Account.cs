using System;
using System.Globalization;

namespace BankServer
{
    public class Account
    {

        private static int _sequence;

        private readonly string _owner;

        public string Owner
        {
            get { return _owner; }
        }

        private readonly string _number;

        public string Number
        {
            get { return _number;  }
        }

        public double Balance { get; private set; }

        public bool IsActive { get; set; }

		public Account(String owner) {
		    Balance = 0;
		    _owner = owner;
		    _number = _sequence++.ToString(CultureInfo.InvariantCulture);
		    IsActive = true;
		}


		public void Deposit(double amount) {
            if (!IsActive)
            {
				throw new InactiveException();
			}
			
			if (amount < 0) {
				throw new ArgumentException();
			}
			
			Balance += amount;
		}

		public void Withdraw(double amount) {
			if (!IsActive) {
				throw new InactiveException();
			}
			
			if (amount < 0) {
				throw new ArgumentException("");
			}
			
			if (amount > Balance) {
				throw new OverdrawException();
			}
			
			Balance -= amount;
		}

	}
}
