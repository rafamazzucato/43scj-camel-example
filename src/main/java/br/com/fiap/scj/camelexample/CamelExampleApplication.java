package br.com.fiap.scj.camelexample;

import br.com.fiap.scj.camelexample.beans.MyObject;
import br.com.fiap.scj.camelexample.services.ObjectService;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;

@SpringBootApplication
public class CamelExampleApplication {

	@Value("${server.port}")
	private String serverPort;

	@Value("${fiapscj.api.path}")
	private String contextPath;

	public static void main(String[] args) {
		SpringApplication.run(CamelExampleApplication.class, args);
	}

	@Bean
	ServletRegistrationBean createServletRegistrationBean(){
		ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(),
				contextPath+"/*");
		servlet.setName("CamelServlet");
		return servlet;
	}

	@Component
	class RestComponent extends RouteBuilder{

		@Override
		public void configure() throws Exception {
			CamelContext context = new DefaultCamelContext();
			restConfiguration()
					.contextPath(contextPath)
					.port(serverPort)
					.enableCORS(true)
					.apiContextPath("/api-doc")
					.apiProperty("api.title", "Camel Springboot Example")
					.apiProperty("api.version", "v1")
					.apiProperty("cors", "true")
					.apiContextRouteId("api-doc")
					.component("servlet")
					.bindingMode(RestBindingMode.json)
					.dataFormatProperty("prettyPrint", "true");

			rest("/api")
					.description("Test Camel Service")
					.id("api-route")
					.post("/test")
					.produces(MediaType.APPLICATION_JSON)
					.consumes(MediaType.APPLICATION_JSON)
					.bindingMode(RestBindingMode.auto)
					.type(MyObject.class)
					.enableCORS(true)
					.to("direct:remoteService");

			from("direct:remoteService")
					.routeId("direct-route")
					.log(">>> ${body.id}")
					.log(">>> ${body.name}")
					.process(new Processor() {
						@Override
						public void process(Exchange exchange) throws Exception {
							MyObject obj = (MyObject) exchange.getIn().getBody();
							ObjectService.execute(obj);
							exchange.getIn().setBody(obj);
						}
					})
					.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));
		}
	}


}
