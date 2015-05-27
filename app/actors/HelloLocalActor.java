package actors;

import akka.actor.UntypedActor;
import com.geekcap.informit.akka.MyMessage;

/**
 * Local Hello, World Actor
 */
public class HelloLocalActor extends UntypedActor
{
	@Override
	public void onReceive( Object message ) throws Exception 
	{
		if( message instanceof MyMessage )
		{
			MyMessage myMessage = ( MyMessage )message;
			myMessage.setMessage( "Local Hello, " + myMessage.getMessage() );
			getSender().tell( myMessage, getSelf() );
		}
		else
		{
			unhandled( message );
		}
	}

}
