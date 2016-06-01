package agents;

import java.io.PrintWriter;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import logic.Product;

public class Auctioneer extends Agent {
	
	public enum AucioneerState {
		START_CN, /* Starting the contract net - initial message */
		WAITING_FOR_RESPONSES,
		SELLING,
		REDUCING_PRICE,
		NEXT_ITEM,
		END, //send message to end auction
		STOP //stop agent
	}
	
	private AucioneerState state;
	private DFAgentDescription[] participants;
	private ArrayList<Product> productsToSell;
	private int productBeingSoldIndex;
	private Product productExample = new Product("sardinhas", 2.0, 4.0, 0.5, 2.5, 200);
	private int currentTurn;
	private int totalTurns;
	
	public Auctioneer(ArrayList<Product> products) {
		productsToSell = products;
		this.productBeingSoldIndex = 0;
		setNumberOfTurns();
	}
	
	protected void setup() {
		System.out.println("Starting auctioneer agent..." + getAID() + " " + getLocalName());
		state = AucioneerState.START_CN;
		initialize();
	}
	
	protected void takeDown() {
		System.out.println("Ending auctioneer agent..." + getAID() + " " + getLocalName());
		state = AucioneerState.END;
	}
	
	protected void setNumberOfTurns() {
		Product p = productsToSell.get(productBeingSoldIndex);
		totalTurns = (int) (((p.getStartPrice() - p.getMinPrice()) / p.getIncrement()) + 1);
		System.out.println("Total turns - "+totalTurns);
		currentTurn = 1;
	}
	
	public int getHowManyTurnsLeft() {
		return totalTurns - currentTurn;
	}
	
	public void initialize() {
	
		//add contract net behaviour 
		addBehaviour(new AuctioneerBehaviour(this));

	}

	public DFAgentDescription[] getParticipants() {
		DFAgentDescription[] result = null;
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("buyer");
		dfd.addServices(sd);
		
		try {
			result = DFService.search(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		participants = result;
		
		return participants;
	}
	
	public void replyBackToAgent(ACLMessage originalMsg, String message, int messageType) {
		ACLMessage msg = originalMsg.createReply();
		msg.setPerformative(messageType);
		if(!message.isEmpty()) {
			msg.setContent(message);
		}
		this.send(msg);
	}
	
	public void sendMessageToAgents(DFAgentDescription[] receivers, String message, int messageType) {
		ACLMessage msg = new ACLMessage(messageType);		
		for(DFAgentDescription receiver : receivers) {
			msg.addReceiver(new AID(receiver.getName().getLocalName(), false));
		}
		//msg.setLanguage("PT");
		msg.setContent(message);
		this.send(msg);
	}
	
	public int nextProduct() {
		productBeingSoldIndex++;
		
		if(productBeingSoldIndex >= productsToSell.size()) {
			productBeingSoldIndex = -1;
			totalTurns = 0;
		}
		else {
			setNumberOfTurns();
		}
		
		return productBeingSoldIndex;
	}
	
	public Product getProductBeingSold() {
		return productsToSell.get(productBeingSoldIndex);
	}
	
	public boolean canSellCurrentProduct(int quantity) {
		return getProductBeingSold().isPossibleToSell(quantity);
	}
	
	public void sellCurrentProduct(int quantity) {
		getProductBeingSold().sell(quantity);
	}
	
	public int getCurrentProductQuantityLeft() {
		return getProductBeingSold().getQuantityAvailable();
	}
	
	public boolean reduceCurrentProductPrice() {
		if(getProductBeingSold().isAcceptablePrice()) {
			getProductBeingSold().reducePrice();
			currentTurn++;
			
			if(currentTurn > totalTurns)
				currentTurn = totalTurns;
			
			return true;
		}
		return false;
	}

	public void printInformation() {
		StringBuilder sb = new StringBuilder();
		sb.append("================AUCTIONEER INFORMATION================\r\n");
		for(Product p : productsToSell) {
			sb.append(p.printFinalInformation());
		}
		sb.append("======================================================\r\n");
		
		PrintWriter out = null;
		try {
			out = new PrintWriter("auctioneer.txt");
			out.write(sb.toString());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			out.close();
		}
	}
	
	public void setParticipants(DFAgentDescription[] participants) {
		this.participants = participants;
	}
	

	public AucioneerState getAucState() {
		return state;
	}

	public void setAucState(AucioneerState state) {
		this.state = state;
	}

	public Product getProductExample() {
		return productExample;
	}
	
	public int getProductBeingSoldIndex() {
		return productBeingSoldIndex;
	}

	public void setProductBeingSoldIndex(int productBeingSoldIndex) {
		this.productBeingSoldIndex = productBeingSoldIndex;
	}

	public ArrayList<Product> getProductsToSell() {
		return productsToSell;
	}

	public void setProductsToSell(ArrayList<Product> productsToSell) {
		this.productsToSell = productsToSell;
	}

	public int getTotalTurns() {
		return totalTurns;
	}

	public void setTotalTurns(int totalTurns) {
		this.totalTurns = totalTurns;
	}

	public int getCurrentTurn() {
		return currentTurn;
	}
	
	public void setCurrentTurn(int currentTurn) {
		this.currentTurn = currentTurn;
	}

}