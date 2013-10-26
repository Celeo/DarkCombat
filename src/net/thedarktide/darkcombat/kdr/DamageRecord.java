package net.thedarktide.darkcombat.kdr;

/**
 * @author Celeo
 */
public class DamageRecord
{

	public Type type;
	public String identifier;

	public DamageRecord(Type t, String id)
	{
		this.type = t;
		this.identifier = id;
	}

	public static enum Type
	{
		PLAYER,
		ENTITY,
		ENVIRONMENT;
	}

	public boolean equals(DamageRecord dr)
	{
		return (dr.type.equals(type) && dr.identifier.equalsIgnoreCase(identifier));
	}

}