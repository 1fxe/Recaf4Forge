package dev.fxe.recaf4forge;

import me.coley.recaf.mapping.MappingImpl;

import java.util.HashMap;

public class Versions {

	public static final HashMap<String, MappingImpl> MAP = new HashMap<String, MappingImpl>(){
		{
			this.put("1.7.10.Reobf", MappingImpl.SRG);
			this.put("1.7.10", MappingImpl.SRG);
			this.put("1.8", MappingImpl.SRG);
			this.put("1.8.9", MappingImpl.SRG);
			this.put("1.9.4", MappingImpl.SRG);
			this.put("1.12.2", MappingImpl.TSRG);
		}
	};

}
