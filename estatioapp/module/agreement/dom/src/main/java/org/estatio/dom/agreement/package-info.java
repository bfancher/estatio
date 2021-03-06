/**
 * Defines {@link org.estatio.dom.agreement.Agreement}, which associates a number of 
 * {@link org.estatio.dom.party.Party party} playing various {@link org.estatio.dom.agreement.AgreementRole role}s.
 * 
 * <p>
 * Agreement itself is abstract, with its associated {@link org.estatio.dom.agreement.type.AgreementType type} acting
 * as a powertype to the subclass.  The type subtypes are <tt>Lease</tt> (representing the occupancy of a unit in 
 * a property) and <tt>BankMandate</tt> (a mechanism by which one party pays another for services).  Thus, two parties 
 * will often have two related but independent {@link org.estatio.dom.agreement.Agreement}s, one being a 
 * <tt>BankMandate</tt>, the other the <tt>Lease</tt>.  

 * <p>
 * Typical roles are <i>PROPERTY_OWNER</i> or <i>ASSET_MANAGER</i> (for <tt>Lease</tt> agreements, say).
 * The party acting in a given role can change over time.
 *
 * <p>
 * Every {@link org.estatio.dom.agreement.Agreement} has a 
 * {@link org.estatio.dom.agreement.Agreement#getPrimaryParty() primary} and a
 * {@link org.estatio.dom.agreement.Agreement#getSecondaryParty() secondary} party; which role identifies these
 * is dependent on the {@link org.estatio.dom.agreement.Agreement#getType() type}.  
 */
package org.estatio.dom.agreement;