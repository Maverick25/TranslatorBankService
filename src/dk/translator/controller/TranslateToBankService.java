/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.translator.controller;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import dk.translator.dto.ConvertedLoanRequestDTO;
import dk.translator.dto.LoanRequestDTO;
import dk.translator.messaging.Receive;
import java.io.IOException;
import java.util.HashMap;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author marekrigan
 */
public class TranslateToBankService 
{
    private static Gson gson;
    private static final String REPLY_QUEUE_NAME = "queue_normalizerBankService";
    public static void receiveMessages() throws IOException,InterruptedException
    {
        gson = new Gson();
        
        HashMap<String,Object> objects = Receive.setUpReceiver();
        
        QueueingConsumer consumer = (QueueingConsumer) objects.get("consumer");
        Channel channel = (Channel) objects.get("channel");
        
        LoanRequestDTO loanRequestDTO;
        ConvertedLoanRequestDTO convertedLoanRequestDTO;
        
        while (true) 
        {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());

            AMQP.BasicProperties props = delivery.getProperties();
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder().correlationId(props.getCorrelationId()).replyTo(REPLY_QUEUE_NAME).build();         
            
            String routingKey = delivery.getEnvelope().getRoutingKey();

            System.out.println(" [x] Received '" + routingKey + "':'" + message + "'");
            
            loanRequestDTO = gson.fromJson(message, LoanRequestDTO.class);

            StringBuilder sb = new StringBuilder(loanRequestDTO.getSsn());
            sb.deleteCharAt(6);
            long convertedSsn = Long.parseLong(sb.toString());

            convertedLoanRequestDTO = new ConvertedLoanRequestDTO(convertedSsn, loanRequestDTO.getLoanAmount(), loanRequestDTO.getLoanDuration(), loanRequestDTO.getCreditScore());

            getBankService(gson.toJson(convertedLoanRequestDTO),gson.toJson(replyProps));

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
        

    /**
     * Get Bank Service Implementation
     * @param request
     * @param props
     */
    public static void getBankService(String request, String props)
       {
           try {
               // Create SOAP Connection
               SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
               SOAPConnection soapConnection = soapConnectionFactory.createConnection();

               // Send SOAP Message to SOAP Server
               String url = "http://localhost:8080/BankWebService/BankWebService";
               SOAPMessage soapResponse = soapConnection.call(createBankRequest(request,props), url);

               // Process the SOAP Response
               assignInventoryResponse(soapResponse);

               soapConnection.close();
           } catch (Exception e) {
               System.err.println("Error occurred while sending SOAP Request to Server");
               e.printStackTrace();
           }
       }

       private static SOAPMessage createBankRequest(String request, String props) throws Exception 
       {
           MessageFactory messageFactory = MessageFactory.newInstance();
           SOAPMessage soapMessage = messageFactory.createMessage();
           SOAPPart soapPart = soapMessage.getSOAPPart();

           String serverURI = "http://service.bankservice.com/";

           // SOAP Envelope
           SOAPEnvelope envelope = soapPart.getEnvelope();
           envelope.addNamespaceDeclaration("bankservice", serverURI);

           // SOAP Body
           SOAPBody soapBody = envelope.getBody();
           SOAPElement quote = soapBody.addChildElement("generateQuote", "bankservice");
           
           SOAPElement requestElement = quote.addChildElement("request");
           requestElement.addTextNode(request);
           SOAPElement propsElement = quote.addChildElement("props");
           propsElement.addTextNode(props);

           soapMessage.saveChanges();

           /* Print the request message */
           System.out.print("Request SOAP Message = ");
           soapMessage.writeTo(System.out);
           System.out.println();

           return soapMessage;
       }

       /**
        * Method used to assign the SOAP Response
        */
       private static void assignInventoryResponse(SOAPMessage soapResponse) throws Exception 
       {
           TransformerFactory transformerFactory = TransformerFactory.newInstance();
           Transformer transformer = transformerFactory.newTransformer();
           javax.xml.transform.Source sourceContent = soapResponse.getSOAPPart().getContent();
           Document doc = soapResponse.getSOAPBody().extractContentAsDocument();

           NodeList node = doc.getElementsByTagName("return");
           String nodeValue = node.item(0).getTextContent();

           System.out.print("\nResponse SOAP Message = ");
           StreamResult result = new StreamResult(System.out);
           transformer.transform(sourceContent, result);
       }
 
}
