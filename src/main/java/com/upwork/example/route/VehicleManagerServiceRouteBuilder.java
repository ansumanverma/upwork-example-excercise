package com.upwork.example.route;

import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.jose4j.jwt.JwtClaims;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.upwork.example.Util;
import com.upwork.example.domain.User;
import com.upwork.example.domain.Vehicle;
import com.upwork.example.domain.Vehicles;
import com.upwork.example.jwt.JWTAuthManager;


@Component
@EnableTransactionManagement
public class VehicleManagerServiceRouteBuilder extends AbstractRestServiceRouteBuilder{

	EntityManager em;
	@PersistenceUnit
	EntityManagerFactory emf;

	@Override
	public void configure() throws RuntimeCamelException {
		// TODO Auto-generated method stub
		super.configure();

		em = emf.createEntityManager();
		onException(Exception.class).
		handled(true).
		process(new Processor() {
			public void process(Exchange exchange) throws Exception {
				Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
				log.error("Redelivery on Exception: ", exception);
				em.getTransaction().rollback();
			}
		}).
		log(LoggingLevel.ERROR, "Error calling ${exception}")
		;

		rest("/v1/getToken")
		.get().to("direct:getToken");

		rest("/v1/viewvehicles")
		.get().to("direct:viewvehicles");

		rest("/v1/viewvehicle")
		.get("/{id}").to("direct:getVehicle");


		rest("/v1/addvehicle")
		.put().to("direct:addvehicle")
		;
		rest("/v1/editvehicle")
		.put("/{id}").to("direct:editvehicle")
		;

		rest("/v1/deletevehicle")
		.delete("/{id}").to("direct:deletevehicle")
		;

		from("direct:viewvehicles").id("ListVehiclesRoute")
		.process(new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {

				em.clear();
				em.getTransaction().begin();

				TypedQuery<Vehicle> query;
				em.getEntityManagerFactory().getCache().evictAll();
				query = em.createNamedQuery("Vehicle.findAll", Vehicle.class);

				List<Vehicle> listVehicle = query.getResultList();

				exchange.getIn().setBody(new Vehicles(listVehicle));
				em.getTransaction().commit();
			}
		});



		from("direct:getVehicle").id("GetVehicleRoute")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Integer id = exchange.getIn().getHeader("id", Integer.class);
				TypedQuery<Vehicle> query = em.createNamedQuery("Vehicle.getVehicle", Vehicle.class);
				query.setParameter("id", id);
				Vehicle vehicle = query.getSingleResult();
				exchange.getIn().setBody(vehicle);
				em.getTransaction().commit();
			}
		})
		;



		from("direct:getToken").id("GetToken")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Message in = exchange.getIn();
				User user = new User();
				user.setUsername("anshuman");
				user.setPassword("anshuman");
				String token = JWTAuthManager.getInstance().generateToken(user);
				HashMap<String, Object> resp = new HashMap<>();
				resp.put("token", token);
				in.setBody(resp);
				in.setHeader(Exchange.CONTENT_TYPE, "application/json");

			}
		})
		;


		from("direct:editvehicle").id("EditVehicleRoute")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Message in = exchange.getIn();
				String authorization = in.getHeader("Authorization", String.class);
				JwtClaims jwtClaims = JWTAuthManager.getInstance().validateAuthBearer(authorization);

				if (jwtClaims == null) {
					in.setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
					in.setBody(null);
				} 

			}
		}) .choice()
		.when(new Predicate() {
			@Override
			public boolean matches(Exchange exchange) {

				Message in = exchange.getIn();
				Integer code = in.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
				Integer code401 = 401;
				return !code401.equals(code);
			}
		})
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {

				em.clear();
				em.getTransaction().begin();
				em.getEntityManagerFactory().getCache().evictAll();
				Integer id = exchange.getIn().getHeader("id", Integer.class);
				TypedQuery<Vehicle> query = em.createNamedQuery("Vehicle.getVehicle", Vehicle.class);
				query.setParameter("id", id);
				Vehicle vehicle = query.getSingleResult();
				em.getTransaction().commit();
				vehicle.setDescription(Util.getRandomlyName(50));
				em.getTransaction().begin();
				em.persist(vehicle);
				em.flush();
				em.getTransaction().commit();
				exchange.getIn().setBody(vehicle);
			}
		})
		;

		from("direct:deletevehicle").id("DeleteVehicleRoute")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Message in = exchange.getIn();
				String authorization = in.getHeader("Authorization", String.class);
				JwtClaims jwtClaims = JWTAuthManager.getInstance().validateAuthBearer(authorization);

				if (jwtClaims == null) {
					in.setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
					in.setBody(null);
				} 

			}
		}) .choice()
		.when(new Predicate() {
			@Override
			public boolean matches(Exchange exchange) {

				Message in = exchange.getIn();
				Integer code = in.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
				Integer code401 = 401;
				return !code401.equals(code);
			}
		})
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {

				em.clear();
				em.getTransaction().begin();
				em.getEntityManagerFactory().getCache().evictAll();
				Integer id = exchange.getIn().getHeader("id", Integer.class);
				TypedQuery<Vehicle> query = em.createNamedQuery("Vehicle.getVehicle", Vehicle.class);
				query.setParameter("id", id);				Vehicle vehicle = query.getSingleResult();
				em.getTransaction().commit();
				vehicle.setDescription(Util.getRandomlyName(50));
				em.getTransaction().begin();
				em.remove(vehicle);
				em.flush();
				em.getTransaction().commit();
			}
		})
		;

		from("direct:addvehicle").id("AddVehicleRoute")
		.process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Message in = exchange.getIn();
				String authorization = in.getHeader("Authorization", String.class);
				JwtClaims jwtClaims = JWTAuthManager.getInstance().validateAuthBearer(authorization);

				if (jwtClaims == null) {
					in.setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
					in.setBody(null);
				} 

			}
		}) .choice()
		.when(new Predicate() {
			@Override
			public boolean matches(Exchange exchange) {

				Message in = exchange.getIn();
				Integer code = in.getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
				Integer code401 = 401;
				return !code401.equals(code);
			}
		}).process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				Vehicle vehicle = new Vehicle();
				vehicle.setDescription(Util.getRandomlyName(50));
				vehicle.setItem(Util.getRandomlyName(10));
				em.getTransaction().begin();
				em.persist(vehicle);
				em.flush();
				em.getTransaction().commit();
				exchange.getIn().setBody(vehicle);
			}

		})
		;
	}
}
