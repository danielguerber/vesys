using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace BankServer
{
    class BankServer
    {
        private readonly TcpClient _client;
        private readonly Bank _bank;

        public BankServer(TcpClient client, Bank bank)
        {
            _client = client;
            _bank = bank;
        }

        public void Read()
        {
            var reader = new StreamReader(_client.GetStream(), Encoding.Unicode);
            var writer = new StreamWriter(_client.GetStream(), Encoding.Unicode);
            var input = reader.ReadLine();
            while (!String.IsNullOrEmpty(input))
            {
                string number;
                Account account;
                switch (input)
                {
                    case "get-acc-numbers":
                        var accNumbers = _bank.GetAccountNumbers().ToArray();
                        writer.WriteLine(accNumbers.Length);
                        foreach (var accNumber in accNumbers)
                        {
                            writer.WriteLine(accNumber);
                        }
                        break;
                    case "create":
                        var owner = reader.ReadLine();
                        number = _bank.CreateAccount(owner);
                        if (!String.IsNullOrEmpty(number))
                        {
                            writer.WriteLine("ok");
                            writer.WriteLine(number);
                        }
                        else
                        {
                            writer.WriteLine("error");
                        }
                        break;
                    case "close":
                        number = reader.ReadLine();
                        writer.WriteLine(_bank.CloseAccount(number) ? "ok" : "error");
                        break;
                    case "get-acc":
                        number = reader.ReadLine();
                        account = _bank.GetAccount(number);
                        if (account != null)
                        {
                            writer.WriteLine("ok");
                            writer.WriteLine(account.Number);
                        }
                        else
                        {
                            writer.WriteLine("error");
                        }
                        break;
                    case "transfer":
                        var fromAccount = _bank.GetAccount(reader.ReadLine());
                        var toAccount = _bank.GetAccount(reader.ReadLine());

                        if (fromAccount == null && toAccount == null)
                        {
                            writer.WriteLine("ArgumentException");
                        }
                        else
                        {
                            try
                            {
                                _bank.Transfer(fromAccount, toAccount, Double.Parse(reader.ReadLine()));
                                writer.WriteLine("ok");
                            }
                            catch (Exception e)
                            {
                                writer.WriteLine(e.GetType().Name);
                            }
                        }
                        break;
                    case "get-balance":
                        number = reader.ReadLine();
                        account = _bank.GetAccount(number);
                        writer.WriteLine(account != null ? account.Balance : Double.NaN);
                        break;
                    case "get-owner":
                        number = reader.ReadLine();
                        account = _bank.GetAccount(number);
                        writer.WriteLine(account != null ? account.Owner : "");
                        break;
                    case "get-active":
                        number = reader.ReadLine();
                        account = _bank.GetAccount(number);
                        writer.WriteLine(account != null && account.IsActive);
                        break;
                    case "deposit":
                        number = reader.ReadLine();
                        account = _bank.GetAccount(number);
                        if (account != null)
                        {
                            try
                            {
                                account.Deposit(Double.Parse(reader.ReadLine()));
                                writer.WriteLine("ok");
                            }
                            catch (Exception e)
                            {
                                writer.WriteLine(e.GetType().Name);
                            }
                        }
                        else
                        {
                            writer.WriteLine("ArgumentException");
                        }
                        break;
                    case "withdraw":
                        number = reader.ReadLine();
                        account = _bank.GetAccount(number);
                        if (account != null)
                        {
                            try
                            {
                                account.Withdraw(Double.Parse(reader.ReadLine()));
                                writer.WriteLine("ok");
                            }
                            catch (Exception e)
                            {
                                writer.WriteLine(e.GetType().Name);
                            }
                        }
                        else
                        {
                            writer.WriteLine("ArgumentException");
                        }
                        break;
                    default:
                        writer.WriteLine("error");
                        break;
                }
                writer.Flush();
                input = reader.ReadLine();
            }
        }

        static void Main(string[] args)
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
    }
}
