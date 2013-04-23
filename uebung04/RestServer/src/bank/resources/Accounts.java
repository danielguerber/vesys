package bank.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import bank.implementation.Account;
import bank.implementation.Bank;
import bank.types.AccountURLs;

import com.sun.jersey.spi.resource.Singleton;

@Path("/accounts")
@Singleton
public class Accounts {
	
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getAccountURLs(@Context Request request, @Context UriInfo uri) {
		EntityTag modified = new EntityTag(String.valueOf(Bank.modified));
		
		ResponseBuilder builder = request.evaluatePreconditions(modified);
		if (builder!= null) {
			return builder.build();
		}
		
		AccountURLs urls = new AccountURLs();
		for (Account account : Bank.accounts.values()) {
			if (account.isActive()) {
				urls.getUrl().add(uri.getRequestUri().toString() + "/" + account.getNumber());
			}
		}
		
		builder = Response.ok(urls);
		builder.tag(modified);
		return builder.build();
	}
	
	@POST
	@Consumes("text/plain")
	public Response createAccount( @Context UriInfo uri, String owner) {
		Account account = new Account(owner);
		Bank.modified++;
		Bank.accounts.put(account.getNumber(), account);
		ResponseBuilder builder;
		builder = Response.created(uri.getRequestUri().resolve(account.getNumber()));
		return builder.build();
	}
	
	@GET
	@Path("{id}")
	public Response getAccount(@PathParam("id") String number) {
		Account account = Bank.accounts.get(number);
		
		if (account==null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		else return Response.ok(account.getAccountData()).build();
	}
	
	@DELETE
	@Path("{id}")
	public Response deleteAccount(@Context Request request, @PathParam("id") String number) {
		Account account = Bank.accounts.get(number);
		
		if (account==null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		synchronized(account) {
			if (account.getBalance() <= 0.0) {
				account.setActive(false);
				Bank.modified++;
			} else {
				return Response.status(Status.CONFLICT).entity("Balance is not 0").build();
			}
		}
		
		return Response.status(Status.NO_CONTENT).build();
	}
	
	@HEAD
	@Path("{id}")
	public Response isActive(@PathParam("id") String number) {
		Account account = Bank.accounts.get(number);
		
		if (account==null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		if (account.isActive()) {
			return Response.ok().build();
		} else {
			return Response.status(Status.GONE).build();
		}
	}
	
	@PUT
	@Path("{id}")
	@Consumes("text/plain")
	public Response setBalance(@Context Request request, @PathParam("id") String number, String sAmount) {
		
		double amount;
		
		amount = Double.parseDouble(sAmount);
		
		Account account = Bank.accounts.get(number);
		
		if (account==null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		synchronized (account) {
			ResponseBuilder builder = request.evaluatePreconditions(new EntityTag(String.valueOf(account.getModified())));
			if (builder != null) {
				builder.build();
			}
			
			account.setBalance(amount);
		}
		
		return Response.status(Status.NO_CONTENT).build();
	}
}
