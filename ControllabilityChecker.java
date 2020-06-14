//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.SynthesisObservationEquivalenceTRSimplifier;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.*;


/**
 * <P>A dummy implementation of a controllability checker.</P>
 *
 * <P>The {@link #run()} method of this model checker does nothing,
 * and simply claims that every model is controllable.</P>
 *
 * <P>You are welcome to edit this file as much as you like,
 * but please <STRONG>do not change</STRONG> the public interface.
 * Do not change the signature of the constructor,
 * or of the {@link #run()} or {@link #getCounterExample()} methods.
 * You should expect a single constructor call, followed by several calls
 * to {@link #run()} and {@link #getCounterExample()}, so your code needs
 * to be reentrant.</P>
 *
 * <P><STRONG>WARNING:</STRONG> If you do not comply with these rules, the
 * automatic tester may fail to run your program, resulting in 0 marks for
 * your assignment.</P>
 *
 * @see ModelChecker
 *
 * @author Robi Malik
 */

public class ControllabilityChecker extends ModelChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new controllability checker to check a particular model.
   * @param  model      The model to be checked by this controllability
   *                    checker.
   * @param  desFactory Factory used for trace construction.
   */
  public ControllabilityChecker(final ProductDESProxy model,
                                final ProductDESProxyFactory desFactory)
  {
    super(model, desFactory);
  }


  //#########################################################################
  //# Invocation
  /**
   * Runs this controllability checker.
   * This method starts the model checking process on the model given
   * as parameter to the constructor of this object. On termination,
   * if the result is false, a counterexample can be queried using the
   * {@link #getCounterExample()} method.
   * Presently, this is a dummy implementation that does nothing but always
   * returns <CODE>true</CODE>.
   * @return <CODE>true</CODE> if the model is controllable, or
   *         <CODE>false</CODE> if it is not.
   */
  @Override
  public boolean run()
  {
    // The following code determines and prints the number of uncontrollable
    // events in each finite-state machine in the model.
    // This is not very helpful for a controllability check,
    // but it demonstrates the use of the interfaces.

    // Start new line (for tidier output with ControllabilityMain)
    System.out.println();

    // First get the model
    final ProductDESProxy model = getModel();

    // Arraylist to hold the number of bits required to pack each automaton's space
    //  index is in the order the automata appear in the for search
    ArrayList<Long> bitsList = new ArrayList<Long>();

    // tuples
    ArrayList<Long> Tuple = new ArrayList<Long>();
    long initialTuple = 0;
    // A string holding the binary representation of the tuple
    String tupleString = "";
    //The total number of states in the model
    int totalStates = 0;
    // The statespace
    ArrayList<Long> stateSpace = new ArrayList<Long>();
    ArrayList<ArrayList<Long>> Qspace = new ArrayList<ArrayList<Long>>();
    // Index of the next unvisited item
    int unvisited = 0;

    //Getting the automata into an order where plants occur first, then specs, properties are discarded
    Set<AutomatonProxy> autos = model.getAutomata();
    AutomatonProxy[] temp = new AutomatonProxy[model.getAutomata().size()];
    temp = autos.toArray(temp);
    //AutomatonProxy[] aps = new AutomatonProxy[model.getAutomata().size()];

    ArrayList<AutomatonProxy> aps = new ArrayList<AutomatonProxy>();
    StateProxy[] sps;

    //int upto = 0;
    Boolean plants = true;
    for (int i = 0; i < temp.length; i++){
      //System.out.println("i " + i + " kind " + temp[i].getKind() + " plants " + plants);
      //System.out.println("temp " + temp[i].getName());
      if (plants && temp[i].getKind()==ComponentKind.PLANT){
        //System.out.println("i: " + i + " plants: "+ plants );
        //System.out.println("add plant");
        //apsl.add(temp[i]);
        aps.add (temp[i]);
        //upto++;
        if (i + 1 == autos.size()){
          i = -1;
          plants = false;
        }
      }
      else if (!plants && temp[i].getKind()==ComponentKind.SPEC){
        //System.out.println("i: " + i + " plants: "+ plants );
        //System.out.println("add spec");
        //aps[upto] = temp[i];
        aps.add(temp[i]);
        //upto++;
        plants = false;
      }
      else if (plants && temp[i].getKind()==ComponentKind.SPEC && i + 1 == temp.length){
        //System.out.println("i: " + i + " plants: "+ plants );
        //System.out.println("specs now");
        i = -1;
        plants = false;
        //System.out.println("i: " + i + " plants: "+ plants );
      }
      //else {System.out.println("UNKNOWN CASE"); System.out.println("i: " + i + " plants: "+ plants + " type: " + temp[i].getKind());}
      //System.out.println("i: " + i + " plants: "+ plants );
    }

    //AutomatonProxy[] aps = new AutomatonProxy[apsl.size()];
    //aps = apsl.toArray(aps);


    // For each automaton ...
    for (int i = 0; i < aps.size(); i++){
      //System.out.println("aps length: " + aps.size());
      //System.out.println("i: " + i);
      //System.out.println("aut " + aps.get(i).getKind());
      AutomatonProxy aut = aps.get(i);
      //System.out.println("states in aut: " + aut.getStates().size());
      //for (final AutomatonProxy aut : aps) {
      // ----------------------------------------------------------------------------------------------------------
      // Add the number of bits required to pad this automata to the array
      bitsList.add((long) Math.floor(Math.log(aut.getStates().size()) / Math.log(2)) + 1);

      //if (aut.getStates().size() == 2){ bitsList.add(1L);}
      //else {
      //  bitsList.add((long) Math.ceil(Math.sqrt(aut.getStates().size())));
      //}
      sps = new StateProxy[aut.getStates().size()];
      sps = aut.getStates().toArray(sps);

      String stateLabel = "";

      //Put the initial states in the initial tuple
      for (int j = 0; j < sps.length; j++){
        //System.out.println("J: " + j);
        if (sps[j].isInitial()){
          //System.out.println("state index" + Long.toBinaryString(j));
          stateLabel = Integer.toBinaryString(j);
          //System.out.println("statelabel: " + stateLabel);
          //tupleString = Integer.toBinaryString(j);
          Tuple.add(Long.parseLong(stateLabel, 2));
          //while(stateLabel.length() < bitsList.get(i)){
          //  stateLabel = "0" + stateLabel;
          // }
          j = sps.length;
        }
        //tupleString = tupleString + stateLabel;
        //totalStates ++;
      }

      //initialTuple = Long.parseLong(tupleString, 2);

      // --------------------------------------------------------------------------------------------------------
      // Print what we have found
      //System.out.println(plantOrSpec + " " + aut.getName() + " has " + count + " uncontrollable events.");
      //System.out.println("States and padding bits:");
      //System.out.println(aut.getStates().size() + " " + bitsList.get(bitsList.size()-1));

      //System.out.println("TupleString: " + tupleString);
      // System.out.println("InitialTuple: " + initialTuple);
    }
    //System.out.println("Initial Tuple: " + initialTuple);
    //System.out.println("Initial Tuple Array: " + Tuple );

    //--------------------------------------------------------------------------------------------------------------------
    Qspace.add(Tuple);

    //long tuple = 0;
    Tuple = new ArrayList<Long>();
    ArrayList<Long> rtuple = new ArrayList<Long>();
    //long rtuple = 0;
    String sTuple;
    Set<EventProxy> alphabet = model.getEvents();

    // Tempory state variables
    StateProxy currState;
    StateProxy nextState;
    // While there are unvisited tuples
    while(unvisited >= 0){
      if(unvisited % 10000 == 0) System.out.println("Checking tuple number " + unvisited);
      Tuple = Qspace.get(unvisited);
      // Mark this tuple as visited
      unvisited ++;

      //Synchronous products and Controllability
      for (EventProxy event : model.getEvents()){
        EventKind ek = event.getKind();

        if (ek == EventKind.UNCONTROLLABLE){
          for (int i = 0; i < aps.size(); i++){
            //Shifting sets into arrays - easier to work with due to index access
            //Set<AutomatonProxy> autos = model.getAutomata();
            //aps = autos.toArray(aps);
            Set<StateProxy> states = aps.get(i).getStates();
            sps = new StateProxy[states.size()];
            sps = states.toArray(sps);

            //System.out.println("States size " + states.size());

            //currState = getState(i, sps, bitsList, binTuple);
            currState = sps[Math.toIntExact(Tuple.get(i))];
            nextState = findSuccessorState(aps.get(i), currState, event);

            if (nextState == currState){//SELF LOOP DO NOTHING
              //System.out.println("Self loop case");
            }
            else if (nextState != null){//ADD TUPLE TO STATESPACE
              //System.out.println("Transition exists");
              rtuple = Tuple;

              ArrayList<StateProxy> spsa = new ArrayList<StateProxy>();
              for(StateProxy sp : sps){
                spsa.add(sp);
              }

              spsa.indexOf(nextState);
              //System.out.println("next index: " + spsa.indexOf(nextState) + " current index: "+ spsa.indexOf(currState));
              //System.out.println("B set: " + rtuple);
              rtuple.set(i, (long) spsa.indexOf(nextState));
              //System.out.println("A set: " + rtuple);

              boolean cont = false;
              boolean diff = false;
              int c =0;

              for (int u = 0; u < Qspace.size(); u++){
                for (Long l : rtuple){
                  c = 0;
                  if (l != Qspace.get(u).get(c)){
                    diff = true;
                    c++;
                    //System.out.println("Tuples diff");
                    break;
                  }
                }
                if (!diff){
                  cont = true;
                  break;
                }
                //else {System.out.println("New Tupple");}
              }

              if (!cont){//!Qspace.contains(rtuple)){
                Qspace.add(rtuple);
                //System.out.println("adding tuple to statespace");
              }
              //else{System.out.println("tuple already exists");}

              if(Qspace.size() % 10000 == 0)System.out.println("Number of tuples: " + Qspace.size());
            }
            else if (aps.get(i).getKind() == ComponentKind.SPEC){
              //UNCONTROLLABLE SO JUMP TO COUNTEREXAMPLE
              //System.out.println("Uncontrollable");

              // Try to compute a counterexample ...
              // This is not yet implemented and should only be done if the model is
              // not controllable, but never mind ...
              mCounterExample = computeCounterExample();

              return false;
            }
            else{
              //Event disabled by plant, Satisfies controllability condition for tuple, jump to next event
              //System.out.println("Next event");
            }
          }
        }
        //ELSE event is controllable so not relevant to automata controllability according to controllability condition
      }

      if (unvisited == Qspace.size()){unvisited = -1;}
    }

    //System.out.println("Total States = " + totalStates);
    //System.out.println("StateSpace size: " + stateSpace.size());

    // This all was no good as far as controllability checking is concerned.
    // Let us just leave.
    return true;
  }

  //#########################################################################
  //  Auxillary Methods

  // -------------------------------------------------------------------------

  // Pack Tuple
  // Returns a bitpacked tuple
  public Long packTuple(long ai, StateProxy[] sps, ArrayList<Long> bitsList, String binTuple, StateProxy nextState){
    //System.out.println("PACKING TUPLE...");
    //System.out.println("Autamaton Index: " + ai + " Binary String Tuple: " + binTuple);
    //System.out.println("Bits Packing: " + bitsList);

    //System.out.println("Binary Tuple: " + binTuple);

    String tuplep1 = "0";
    String tuplep2 = "0";
    String tuplep3 = "0";
    String tupleString = "0";

    // Finding position of state to change
    long sIndex = 0L;
    for (int i = 0; i < ai; i++){
      sIndex += bitsList.get(i);
    }
    long eIndex = sIndex + bitsList.get((int) ai);
    //System.out.println("Start: "+ sIndex + " End: "+ eIndex);

    long numBits = 0;
    for (int i = 0; i < bitsList.size(); i++){
      numBits += bitsList.get(i);
    }
    //System.out.println("NumBits: " + numBits);

    //Creating the full binary string
    String allBits = "";
    for (int i = 0; i < (numBits - binTuple.length()); i++){
      allBits = allBits + "0";
    }
    allBits = allBits + binTuple;
    //System.out.println("Full Tuple: "+ allBits);

    //Seperating the existing tuple
    tuplep1 = allBits.substring(0, (int)sIndex);
    tuplep3 = allBits.substring((int) eIndex);

    // Editing the tuple
    for(int i = 0; i < sps.length; i++){
      if (sps[i] == nextState){
        tuplep2 = Integer.toBinaryString(i);


        while (tuplep2.length() < bitsList.get((int)ai)){
          tuplep2 = "0" + tuplep2;
        }
        i = sps.length;
      }
    }
    //System.out.println("sps length: " + sps.length);
    //System.out.println("New t2: " +tuplep2);

    //System.out.println("Tuple: " + tuplep1 +" "+ tuplep2 +" "+ tuplep3);
    //tupleString = tuplep1 + tuplep2 + tuplep3;
    //System.out.println("tupleString> " + tupleString);
    //System.out.println("Tuple: " + Long.parseLong(tupleString, 2));

    return Long.parseLong(tupleString, 2);
  }

  // Get Autamaton State
  // Returns the state of the automata at the given index in the tuple
  public StateProxy getState(long aIndex, StateProxy[] sps, ArrayList<Long> bitsList, String binTuple){
    //System.out.println("GET STATE...");
    System.out.println("Index: " + aIndex);// " Binary String Tuple: " + binTuple);
    System.out.println("Bits Packing: " + bitsList);
    System.out.println("SPS size " + sps.length);

    // Preparing to extract the desired state
    long sIndex = 0L;
    for (int i = 0; i < aIndex; i++){
      sIndex += bitsList.get(i);
    }
    long eIndex = sIndex + bitsList.get((int) aIndex);
    //System.out.println("Start: "+ sIndex + " End: "+ eIndex);

    long numBits = 0;
    for (int i = 0; i < bitsList.size(); i++){
      numBits += bitsList.get(i);
    }
    //System.out.println("NumBits: " + numBits);

    //Creating the full binary string
    String allBits = "";
    for (int i = 0; i < (numBits - binTuple.length()); i++){
      allBits = allBits + "0";
    }
    allBits = allBits + binTuple;
    System.out.println("Full Tuple: "+ allBits);

    // Extracting he desired state
    String stateString = allBits.substring((int) sIndex, (int) eIndex);
    if(sIndex == eIndex) {
      //System.out.println("start and end equal");
      //System.out.println("bitslist at aIndex " + bitsList.get((int) aIndex));
      for (int i = 0; i < bitsList.get((int) aIndex); i++) {
        char c = allBits.charAt((int) sIndex + i);
        stateString = stateString + Character.toString(c);
        System.out.println("c: " + c);
      }
    }
    System.out.println("State Binary String: "+ stateString);
    long stateIndex = Long.parseLong(stateString, 2);
    //System.out.println("State Index: "+ stateIndex);

    return sps[(int)stateIndex];
  }

  // Successor State
  // Returns the state which follows the current state after a given event
  //  If the event is not allowed, return null
  public StateProxy findSuccessorState(AutomatonProxy aut, StateProxy state, EventProxy event){
    //System.out.println("FINDING SUCCESSOR...");

    Set<EventProxy> alphabet = aut.getEvents();

    //System.out.println("Getting Successor For... AUT: " + aut +" State: "+ state +" Event: "+ event );

    // Implicit Self loop case
    if (!alphabet.contains(event)){
      return state;
    }
    // If there is a transition that fits, return the successor
    for (TransitionProxy trans : aut.getTransitions()){
      if (trans.getSource() == state && trans.getEvent() == event){
        return trans.getTarget();
      }
    }

    //Otherwise the automaton doesn't allow the event
    return null;
  }

  // ----------------------------------------------------------------------
  // Enumerate States
  // Enumerates the states belonging to the automata

  //#########################################################################
  //# Simple Access Methods
  /**
   * Gets a counterexample if the model was found to be not controllable.
   * representing a controllability error trace. A controllability error
   * trace is a nonempty sequence of events such that all except the last
   * event in the list can be executed by the model. The last event in the list
   * is an uncontrollable event that is possible in all plant automata, but
   * not in all specification automata present in the model. Thus, the last
   * step demonstrates why the model is not controllable.
   * @return A trace object representing the counterexample.
   *         The returned trace is constructed for the input product DES
   *         of this controllability checker and shares its automata and
   *         event objects.
   */
  @Override
  public SafetyCounterExampleProxy getCounterExample()
  {
    // Just return a stored counterexample. This is the recommended way
    // of doing this, because we may no longer be able to use the
    // data structures used by the algorithm once the run() method has
    // finished. The counterexample can be computed by a method similar to
    // computeCounterExample() below or otherwise.
    return mCounterExample;
  }

  /**
   * Computes a counterexample.
   * This method is to be called from {@link #run()} after the model was
   * found to be not controllable, and while any data structures from
   * the controllability check that may be needed to compute the
   * counterexample are still available.
   * @return The computed counterexample.
   */
  private SafetyCounterExampleProxy computeCounterExample()
  {
    // The following creates a trace that consists of all the events in
    // the input model.
    // This code is only here to demonstrate the use of the interfaces.
    // IT DOES NOT GIVE A CORRECT COUNTEREXAMPLE!

    final ProductDESProxyFactory desFactory = getFactory();
    final ProductDESProxy des = getModel();
    final String desName = des.getName();
    final String traceName = desName + ":uncontrollable";
    final Collection<EventProxy> events = des.getEvents();
    final List<EventProxy> eventList = new LinkedList<>();
    for (final EventProxy event : events) {
      eventList.add(event);
    }
    return
            desFactory.createSafetyCounterExampleProxy(traceName, des, eventList);
  }


  //#########################################################################
  //# Data Members
  /**
   * The computed counterexample or null if the model is controllable.
   */
  private SafetyCounterExampleProxy mCounterExample;

}
