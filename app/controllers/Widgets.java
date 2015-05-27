package controllers;

import com.fasterxml.jackson.databind.JsonNode;

import models.Widget;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class Widgets extends Controller {
	public static Result getWidget( String id ) {
		Widget widget = Widget.findById( id );
		if( widget != null ) {
			return ok( Json.toJson( widget ) );
		}
		return notFound( "No widget found with id: " + id );
	}
	
	public static Result list() {
		return ok( Json.toJson( Widget.findAll() ) );
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public static Result addWidget() {
		JsonNode jsonNode = Controller.request().body().asJson();
		Widget widget = Json.fromJson( jsonNode, Widget.class );
		widget.save();
		return created();
	}
	
}
