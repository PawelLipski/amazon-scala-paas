package models;

import java.util.ArrayList;
import java.util.List;

public class Widget 
{
	public String id;
	public String name;
	public String description;
	
	public Widget() {
		
	}
	
	public Widget( String id, String name, String description ) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
	
	public static List<Widget> findAll() {
		return widgets;
	}
	
	public void save() {
		widgets.add( this );
	}
	
	public static Widget findById( String id ) {
		for( Widget widget : widgets ) {
			if( widget.id.equalsIgnoreCase( id ) ) {
				return widget;
			}
		}
		return null;
	}
	
	public static List<Widget> widgets = new ArrayList<Widget>();
	static {
		widgets.add( new Widget( "1", "Widget 1", "This is Widget 1" ) );
		widgets.add( new Widget( "2", "Widget 2", "This is Widget 2" ) );
	}
	
}
