using System;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace BankServer
{
    class BankServer
    {
        private readonly TcpClient _client;
        private readonly Bank _bank;
        private readonly StreamReader _reader;
        private readonly StreamWriter _writer;

        public BankServer(TcpClient client, Bank bank)
        {
            _client = client;
            _bank = bank;
            _reader = new StreamReader(_client.GetStream(), Encoding.Unicode);
            _writer = new StreamWriter(_client.GetStream(), Encoding.Unicode);
        }

        private static String Escape(String s)
        {
            return s.Replace("\n", "").Replace(":", "[colon]");
        }

        private static String Unescape(String s)
        {
            return s.Replace("\n", "").Replace("[colon]", ":");
        }

        public void SendMessage(String command, params string[] args) {
			var sb = new StringBuilder(Escape(command));
			for (var i = 0; i < args.Count(); i++) {
				sb.Append(":");
				sb.Append(Escape(args[i]));
			}
			_writer.WriteLine(sb.ToString());
			_writer.Flush();
		}

        public void Read()
        {
            
            String input;
            while ((input = _reader.ReadLine()) != null)
            {
                Account account;
                var message = input.Split(':').Select(Unescape).ToArray();
                
                switch (message[0])
                {
                    case "get-acc-numbers":
                        var accNumbers = _bank.GetAccountNumbers().ToArray();
                        SendMessage(accNumbers.Length.ToString(CultureInfo.InvariantCulture), accNumbers);
                        break;
                    case "create":
                        if (message.Length < 2)
                        {
                            SendMessage("error");
                        }
                        else
                        {
                            var number = _bank.CreateAccount(message[1]);
                            if (!String.IsNullOrEmpty(number))
                            {
                                SendMessage("ok", number);
                            }
                            else
                            {
                                SendMessage("error");
                            }
                        }
                        break;
                    case "close":
                        if (message.Length < 2)
                        {
                            SendMessage("error");
                        }
                        else
                        {
                            SendMessage(_bank.CloseAccount(message[1]) ? "ok" : "error");
                        }
                        break;
                    case "get-acc":
                        if (message.Length < 2)
                        {
                            SendMessage("error");
                        }
                        else
                        {
                            account = _bank.GetAccount(message[1]);
                            if (account != null)
                            {
                                SendMessage("ok", account.Number);
                            }
                            else
                            {
                                SendMessage("error");
                            }
                        }
                        break;
                    case "transfer":
                        if (message.Length < 4)
                        {
                            SendMessage("error");
                        }
                        else
                        {
                            var fromAccount = _bank.GetAccount(message[1]);
                            var toAccount = _bank.GetAccount(message[2]);

                            if (fromAccount == null && toAccount == null)
                            {
                                SendMessage("ArgumentException");
                            }
                            else
                            {
                                try
                                {
                                    _bank.Transfer(fromAccount, toAccount, Double.Parse(message[3]));
                                    SendMessage("ok");
                                }
                                catch (Exception e)
                                {
                                    SendMessage(e.GetType().Name);
                                }
                            }
                        }
                        break;
                    case "get-balance":
                        if (message.Length < 2)
                        {
                            SendMessage(Double.NaN.ToString(CultureInfo.InvariantCulture));
                        }
                        else
                        {
                            account = _bank.GetAccount(message[1]);
                            SendMessage((account != null ? account.Balance : Double.NaN)
                                .ToString(CultureInfo.InvariantCulture));
                        }
                        break;
                    case "get-owner":
                        if (message.Length < 2)
                        {
                            SendMessage("");
                        }
                        else
                        {
                            account = _bank.GetAccount(message[1]);
                            SendMessage(account != null ? account.Owner : "");
                        }
                        break;
                    case "get-active":
                        if (message.Length < 2)
                        {
                            SendMessage(false.ToString());
                        }
                        else
                        {
                            account = _bank.GetAccount(message[1]);
                            SendMessage((account != null && account.IsActive).ToString());
                        }
                        break;
                    case "deposit":
                        if (message.Length < 3)
                        {
                            SendMessage("error");
                        }
                        else
                        {
                            account = _bank.GetAccount(message[1]);
                            if (account != null)
                            {
                                try
                                {
                                    account.Deposit(Double.Parse(message[2]));
                                    SendMessage("ok");
                                }
                                catch (Exception e)
                                {
                                    SendMessage(e.GetType().Name);
                                }
                            }
                            else
                            {
                                SendMessage("ArgumentException");
                            }
                        }
                        break;
                    case "withdraw":
                        if (message.Length < 3)
                        {
                            SendMessage("error");
                        }
                        else
                        {
                            account = _bank.GetAccount(message[1]);
                            if (account != null)
                            {
                                try
                                {
                                    account.Withdraw(Double.Parse(message[2]));
                                    SendMessage("ok");
                                }
                                catch (Exception e)
                                {
                                    SendMessage(e.GetType().Name);
                                }
                            }
                            else
                            {
                                SendMessage("ArgumentException");
                            }
                        }
                        break;
                    default:
                        SendMessage("error");
                        break;
                }
            }
        }

// ReSharper disable FunctionNeverReturns
        static void Main()
        {
            var bank = new Bank();
            var listener = new TcpListener(IPAddress.Any, 5678);
            listener.Start();

            while (true)
            {
                var client = listener.AcceptTcpClient();
                var thread = new Thread(new BankServer(client, bank).Read);
                thread.Start();
            }
        }
// ReSharper restore FunctionNeverReturns
    }
}
