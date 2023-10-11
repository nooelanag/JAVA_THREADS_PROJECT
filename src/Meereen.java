// Made by NOEL ANDOLZ AGUADO and DANIEL LOZANO UCEDA
import java.util.Random;
class Alchemist extends Thread {
	private int id;
	Monitor monitor;
	public Alchemist(int id, Monitor monitor) {
		this.id = id;
		this.monitor = monitor;
	}
	public void run() {
		monitor.enterAntechamber();
		monitor.enterCorridor(this.id);
		//This sleep represents the time taken by the Alchemists to go through the whole corridor.  Is equivalent to one minute.
		try {
			Thread.sleep(60000);
		} catch(Exception e) {}
		monitor.enterLair(id);
		//This sleep represents the time taken by the Alchemists to leave their pots. Is equivalent to ten minutes.
		try {
			Thread.sleep(600000);
		} catch(Exception e) {}
		monitor.exitLair(id);
		//This sleep represents the time taken by the Alchemists to go through the whole corridor. Is equivalent to one minute
		try {
			Thread.sleep(60000);
		} catch(Exception e) {}
		monitor.exitCorridor();
		monitor.exitAntechamber(id);
	}
}
class Dragons extends Thread {
	private String name;
	Monitor monitor;
	public Dragons(String name, Monitor monitor) {
		this.name = name;
		this.monitor = monitor;
	}
	public void run() {
		for(int i = 0; i < 10; i++) {
			//Time before deciding to go inside the Lair.
            try {
                Random rnd = new Random();
                Thread.sleep(rnd.nextInt(60000) + 100);
            } catch(Exception e) {}
            monitor.enterDragon(this.name);
            //Time spent inside the Lair.
            try {
                Random rnd = new Random();
                Thread.sleep(rnd.nextInt(800000) + 100);
            } catch(Exception e) {}
            monitor.leaveDragon(this.name);
        }
	}
}
class Monitor {
	public static final int MAX_ANTECHAMBER = 5;
	public static final int MAX_CORRIDOR = 9;
	public static int MAX_LAIR = 20;
	public static final int GROUPS = 3; //This variable is the number of Alchemists needed to form a group of 3 to go to the Corridor.
	private static final int NONE = -1;
	private static final int ENTER = 0;
	private static final int LEAVE = 1;
	private int alchemistsInAntechamber, alchemistsInCorridor, alchemistsInLair, corridorDirection = NONE;
	private static int wantToEnter;
	private boolean checkGroup = false;
	private boolean DROGON_IN = false;
	private boolean VISERION_IN = false;
	private boolean RHAEGAL_IN = false;
	private int numberOfDragons;
	
	
	//This piece of code is a way to maintain the structure of the messages during the whole program and avoiding to write System.out.println() everytime. This time is about the alchemists.
	public void AlchemistsState(int id, String s) {
		System.out.println("Alchemist #" + id + ": " + s);
	}
	
	//This piece of code is a way to maintain the structure of the messages during the whole program and avoiding to write System.out.println() everytime. This time is about the dragons.
	public void DragonState(String name, String s) {
        System.out.println(name + ": " + s);
    }
	
	//This code is used for alchemists to enter the Antechamber.
    //The first while makes sure that no deadlock is possible.
	synchronized void enterAntechamber() {
		while(alchemistsInAntechamber >= MAX_ANTECHAMBER - 1 || alchemistsInAntechamber + alchemistsInCorridor + alchemistsInLair >= MAX_LAIR) {
			try {
				this.wait();
			} catch(Exception e) {}
		}
		alchemistsInAntechamber++;
		this.notifyAll();
	}
	
	//This code is used by Alchemists to go from the Antechamber to the Corridor.
    //If the number of alchemists wanting to enter the Corridor is not three, then, they will wait until there are three.
	synchronized void enterCorridor(int id) {
		AlchemistsState(id, "I want to enter.");
		while(wantToEnter == GROUPS || checkGroup == true) {
			try {
				this.wait();
			} catch(Exception e) {}
		}
		wantToEnter++;
		if(wantToEnter == GROUPS) {
			checkGroup = true;
		}
		while(checkGroup == false || alchemistsInCorridor >= MAX_CORRIDOR || corridorDirection == LEAVE) {
			try {
				this.wait();
			} catch(Exception e) {}
		}
		
		//By this part, there is a group of three Alchemists wanting to enter the Corridor.
		AlchemistsState(id, "We are three!!");
		wantToEnter--;
		if(wantToEnter == 0)
			checkGroup = false;
		
		//The Alchemists enter the Corridor.
		corridorDirection = ENTER;
		alchemistsInCorridor++;
		alchemistsInAntechamber--;
		AlchemistsState(id, "In the corridor.");
		this.notifyAll();
	}
	
	//This code is used by Alchemists to go from the Corridor to the Lair.
    //To do so, the maximum capacity at that time has to be checked.
    //When the last alchemist from the queue in the corridor gets to the Lair, it will change the direction of the corridor to "NONE"
    //so that other alchemists can change the direction of the Corridor according to what they need.
	synchronized void enterLair(int id) {
		while(alchemistsInLair >= MAX_LAIR) {
			try {
				this.wait();
			} catch(Exception e) {}
		}
		alchemistsInLair++;
		alchemistsInCorridor--;
		AlchemistsState(id, "In the lair");
		if(alchemistsInCorridor == 0) {
			corridorDirection = NONE;
		}
		this.notifyAll();
	}
	
	//This code is used by the Alchemists to go from the Lair back to the Corridor.
    //To be able to do so, the direction of the corridor needs to be the correct one and when that happens,
    //we need to check that the maximum capacity is not exceeded.
	synchronized void exitLair(int id) {
		AlchemistsState(id, "I want to go out.");
		while(alchemistsInCorridor >= MAX_CORRIDOR || corridorDirection == ENTER) {
			try {
				this.wait();
			} catch(Exception e) {}
		}
		corridorDirection = LEAVE;
		alchemistsInCorridor++;
		alchemistsInLair--;
		this.notifyAll();
	}
	
	//This code is used by the Alchemists to go from the Corridor to the Antechamber.Once the last alchemist passes through the corridor, it sets the 
    //direction of it to "NONE".
	synchronized void exitCorridor() {
		while(alchemistsInAntechamber >= MAX_ANTECHAMBER) {
			try {
				this.wait();
			} catch(Exception e) {}
		}
		alchemistsInCorridor--;
		alchemistsInAntechamber++;
		if(alchemistsInCorridor == 0) {
			corridorDirection = NONE;
		}
		this.notifyAll();
	}
	
	//This code is used by the Alchemists to go out from the Antechamber.
	synchronized void exitAntechamber(int id) {
		alchemistsInAntechamber--;
		AlchemistsState(id, "The end.");
		this.notifyAll();
	}

	//This code is used by the Dragons to enter the Lair.
	synchronized void enterDragon(String name) {
		DragonState(name, "I want to enter the lair.");
		//When DROGON tries to enter:
		if(name.equals("Drogon") == true) {
			//If there are more than 0 alchemists, the dragon will wait to be able to enter
			while(alchemistsInLair > 0) {
				try {
					this.wait();
				} catch(Exception e) {}
			}
			DROGON_IN = false;
			//Now, the maximum capacity of the Lair is set to 0 so that from this moment onwards, no alchemists enter the Lair.
			MAX_LAIR = 0;
		}
		//When Viserion tries to enter:
		else if(name.equals("Viserion") == true) {
			//If there are more than 10 alchemists (when neither Rhaegal nor Drogon are inside) or more than 5 when (Drogon is not inside but Rhaegal is)
			//Viserion will wait to be able to enter.
				while((alchemistsInLair > 10 && DROGON_IN == false && RHAEGAL_IN == false) || (alchemistsInLair > 5 && DROGON_IN == false && RHAEGAL_IN == true)) {
					try {
						this.wait();
					} catch(Exception e) {}
				}
				VISERION_IN = true;
				if(RHAEGAL_IN == true && DROGON_IN == false)
					//The maximum capacity is set to 5 as Both Rhaegal and Viserion are inside the Lair.
					MAX_LAIR = 5;
				else if(RHAEGAL_IN == false && DROGON_IN == false)
					//The maximum capacity is set to 10 as only Viserion is inside the Lair. 
					MAX_LAIR = 10;
		}
		//When Rhaegal tries to enter:
		else {
			//If there are more than 10 alchemists(when neither Viserion nor Drogon are inside) or more tha 5 when (Drogon is not inside but Viserion is)
			//Rhaegal will wait to be able to enter.
				while((alchemistsInLair > 10 && DROGON_IN == false && VISERION_IN == false) || (alchemistsInLair > 5 && DROGON_IN == false && VISERION_IN == true)) {
					try {
						this.wait();
					} catch(Exception e) {}
				}
				RHAEGAL_IN = true;
				if(VISERION_IN == true && DROGON_IN == false)
					MAX_LAIR = 5;//The maximum capacity of the Lair is set to 5 as both Viserion and Rhaegal are inside the Lair. 
				else if(VISERION_IN == false && DROGON_IN == false)
					MAX_LAIR = 10;//The maximum capacity of the Lair is set to 10 as only Rhaegal is inside the it. 
		}
		DragonState("-->" + name, alchemistsInLair + "in/" + MAX_LAIR + " max");
		DragonState(name, "I am in.");
		numberOfDragons++;
		this.notifyAll();
	}
	//This code is used by the Dragons to leave the Lair.
	synchronized void leaveDragon(String name) {
		DragonState(name, "I want to leave the lair.");
		if(name.equals("Drogon") == true)
			DROGON_IN = false;
		else if(name.equals("Viserion") == true)
			VISERION_IN = false;
		else
			RHAEGAL_IN = false;
		if(DROGON_IN == true)
			MAX_LAIR = 0;
		else if(VISERION_IN == true && RHAEGAL_IN == true)
			MAX_LAIR = 5;
		else if(VISERION_IN == true || RHAEGAL_IN == true)
			MAX_LAIR = 10;
		else
			MAX_LAIR = 20;
		DragonState(name, "I am out.");
		numberOfDragons--;
		if(numberOfDragons == 0)
			System.out.println("****No dragons");
		this.notifyAll();
	}
}
public class Meereen {
	public static void main(String[] args) {
	    Monitor monitor = new Monitor();
	    for(int i = 0; i < 300; i++) {
	    	Alchemist alchemists = new Alchemist(i, monitor);
	    	alchemists.start();
	    }
	    Dragons DROGON = new Dragons("Drogon", monitor);
	    DROGON.start();
        Dragons VISERION = new Dragons("Viserion", monitor);
        VISERION.start();
        Dragons RHAEGAL = new Dragons("Rhaegal", monitor);
        RHAEGAL.start();
	}
}