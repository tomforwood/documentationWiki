package org.forwoods.docuwiki.documentable;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class EnumRepresentation extends TopLevelDocumentable {
	
	List<EnumConstant> enumValues = new ArrayList<>();
	
	public EnumRepresentation(boolean userGenerated, String name) {
		super(new ObjectType("enum"),userGenerated,name);
	}

	public EnumRepresentation() {
		super(new ObjectType("enum"));
		version = 1;
	}
	
	public static class EnumConstant extends Documentable {
		String name;
		String enumValue;
		
		//JSON constructor
		@SuppressWarnings("unused")
		private EnumConstant() {}
		
		public EnumConstant(String name) {
			this.name = name;
		}

		public String getEnumValue() {
			return enumValue;
		}

		public void setEnumValue(String value) {
			this.enumValue = value;
		}

		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		public boolean equals(Object obj) {
			EnumConstant other = (EnumConstant)obj;
			return this.name.equals(other.name);
		}
	}

	public List<EnumConstant> getEnumValues() {
		return enumValues;
	}
	
	public void addEnumValue(EnumConstant ec) {
		enumValues.add(ec);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((enumValues == null) ? 0 : enumValues.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnumRepresentation other = (EnumRepresentation) obj;
		if (enumValues == null) {
			if (other.enumValues != null)
				return false;
		} else if (!enumValues.equals(other.enumValues))
			return false;
		return true;
	}
}
