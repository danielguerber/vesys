using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace BankServer
{
    public class Account
    {

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

        private double _balance = 0;

        public double Balance 
        { 
            get { return _balance;  }
        }


        public bool IsActive { get; set; }

		public Account(String owner) {
			this._owner = owner;
			this._number = Guid.NewGuid().ToString();
		    IsActive = true;
		}


		public void Deposit(double amount) {
			if (!this.IsActive) {
				throw new InactiveException();
			}
			
			if (amount < 0) {
				throw new ArgumentException();
			}
			
			this._balance += amount;
		}

		public void Withdraw(double amount) {
			if (!this.IsActive) {
				throw new InactiveException();
			}
			
			if (amount < 0) {
				throw new ArgumentException("");
			}
			
			if (amount > _balance) {
				throw new OverdrawException();
			}
			
			this._balance -= amount;
		}

	}
}
