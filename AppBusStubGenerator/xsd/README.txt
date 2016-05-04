Required classes:
- ApplicationInterfaces
- ApplicationInterfacesProperties
- package-info
- TBoolean
- TDocumentation
- TExtensibleElements
- TInterface
- TOperation
- TParameter


Needed adaptations after class generation:

	- ApplicationInterfaces should look like:

			@XmlElement(name = "Interface")
   			 protected List<TInterface> _interface;
   			 
   			 
   	- TExtensibleElements :
   	delete not needed @XmlSeeAlso entries.

