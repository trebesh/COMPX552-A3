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
    //  Redundant wih long arraylist tuples
    //ArrayList<Long> bitsList = new ArrayList<Long>();

    // tuples
    ArrayList<Long> Tuple = new ArrayList<Long>();
    // The statespace
    ArrayList<ArrayList<Long>> Qspace = new ArrayList<ArrayList<Long>>();
    // Index of the next unvisited item
    int unvisited = 0;

    // ORDER AUTOMATA -------------------------------------------------------------------------------------
    //  Getting the automata into an order where plants occur first, then specs, properties are discarded
    Set<AutomatonProxy> autos = model.getAutomata();
    AutomatonProxy[] temp = new AutomatonProxy[model.getAutomata().size()];
    temp = autos.toArray(temp);

    ArrayList<AutomatonProxy> aps = new ArrayList<AutomatonProxy>();
    StateProxy[] sps;

    Boolean plants = true;
    for (int i = 0; i < temp.length; i++){
      if (plants && temp[i].getKind()==ComponentKind.PLANT){
        aps.add (temp[i]);
        if (i + 1 == autos.size()){
          i = -1;
          plants = false;
        }
      }
      else if (!plants && temp[i].getKind()==ComponentKind.SPEC){
        aps.add(temp[i]);
        plants = false;
      }
      else if (plants && temp[i].getKind()==ComponentKind.SPEC && i + 1 == temp.length){
        i = -1;
        plants = false;
      }
    }


    // INITIAL TUPLE ---------------------------------------------------------------------------------------
    for (int i = 0; i < aps.size(); i++){
      AutomatonProxy aut = aps.get(i);

      // Add the number of bits required to pad this automata to the array
      //  functionality lost due to switch to long array representation of tuple
      //bitsList.add((long) Math.floor(Math.log(aut.getStates().size()) / Math.log(2)) + 1);

      sps = new StateProxy[aut.getStates().size()];
      sps = aut.getStates().toArray(sps);

      String stateLabel = "";

      //Put the initial states in the initial tuple
      for (int j = 0; j < sps.length; j++){
        if (sps[j].isInitial()){
          stateLabel = Integer.toBinaryString(j);
          Tuple.add(Long.parseLong(stateLabel, 2));
          j = sps.length;
        }
      }
    }

    Qspace.add(Tuple);

    //long tuple = 0;
    Tuple = new ArrayList<Long>();
    ArrayList<Long> rtuple;
    //long rtuple = 0;
    String sTuple;
    Set<EventProxy> alphabet = model.getEvents();

    // Tempory state variables
    StateProxy currState;
    StateProxy nextState;

    // CONTROLLABILITY ---------------------------------------------------------------------------------
    // While there are unvisited tuples
    while(unvisited >= 0){
      //if(unvisited % 10000 == 0) System.out.println("Checking tuple number " + unvisited);
      Tuple = Qspace.get(unvisited);

      // Mark this tuple as visited
      unvisited ++;

      //Synchronous products and Controllability
      for (EventProxy event : model.getEvents()){
        EventKind ek = event.getKind();

        if (ek == EventKind.UNCONTROLLABLE){
          for (int i = 0; i < aps.size(); i++){
            //Shifting sets into arrays - easier to work with due to index access, ordering
            Set<StateProxy> states = aps.get(i).getStates();
            sps = new StateProxy[states.size()];
            sps = states.toArray(sps);

            currState = sps[Math.toIntExact(Tuple.get(i))];
            nextState = findSuccessorState(aps.get(i), currState, event);

            if (nextState == currState){}//SELF LOOP DO NOTHING
            else if (nextState != null){//ADD TUPLE TO STATESPACE
              rtuple = new ArrayList<Long>(Tuple);

              ArrayList<StateProxy> spsa = new ArrayList<StateProxy>();
              for(StateProxy sp : sps){
                spsa.add(sp);
              }

              spsa.indexOf(nextState);
              rtuple.set(i, (long) spsa.indexOf(nextState));

              boolean cont = false;
              boolean diff = false;
              int c =0;

              // Had issues with the arraylist .contains and .equals methods producing false for matching tuples
              //  so had to write my own checker, probably at a hit to performance time.
              for (int u = 0; u < Qspace.size(); u++){
                for (Long l : rtuple){
                  c = 0;
                  if (l != Qspace.get(u).get(c)){
                    diff = true;
                    c++;
                    break;
                  }
                }
                if (!diff){
                  cont = true;
                  break;
                }
              }

              if (!cont){

                Qspace.add(null);
                Qspace.set(Qspace.size()-1, rtuple);
              }

              if(Qspace.size() % 10000 == 0)System.out.println("Number of tuples: " + Qspace.size());
            }
            else if (aps.get(i).getKind() == ComponentKind.SPEC){

              //Attempt counterexample   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
              //COMMENT OUT THIS LINE FOR CONTROLLABILITY ANALYSIS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
              // UNCOMMENT for attempt at counterexample (i think its an infinite loop) !!!!!!!!!!!!!!!!!!!!!!!!!!!
              //  May have to comment out getCounterExample call in Controllability Main to avoid error
              //mCounterExample = computeCounterExample(model, Qspace, sps, aps.get(i), aps);
              //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

              return false;
            }
            else{}//Event disabled by plant, Satisfies controllability condition for tuple, jump to next event
          }
        }
        //ELSE event is controllable so not relevant to automata controllability according to controllability condition
      }

      if (unvisited == Qspace.size()){unvisited = -1;}
    }

    return true;
  }

  //#########################################################################
  //  Auxillary Methods

  // -------------------------------------------------------------------------
  // Successor State
  // Returns the state which follows the current state after a given event
  //  If the event is not allowed, return null
  public StateProxy findSuccessorState(AutomatonProxy aut, StateProxy state, EventProxy event){
    Set<EventProxy> alphabet = aut.getEvents();
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
  // ATTEMPTED BUT NOT WORKING
  //  Print statements left in as still in development
  //  Gets stuck in infinite loop
  private synchronized SafetyCounterExampleProxy computeCounterExample(ProductDESProxy model, ArrayList<ArrayList<Long>> tuples, StateProxy[] sps, AutomatonProxy aut, ArrayList<AutomatonProxy> aps) {
    final ProductDESProxyFactory desFactory = getFactory();
    final ProductDESProxy des = getModel();
    final String desName = des.getName();
    final String traceName = desName + ":uncontrollable";
    final Collection<EventProxy> events = des.getEvents();
    final List<EventProxy> eventList = new LinkedList<>();

    ArrayList<Long> target = tuples.get(tuples.size() - 1);
    ArrayList<Long> curr = new ArrayList<Long>();
    ArrayList<Long> nexttup = new ArrayList<Long>();
    ArrayList<ArrayList<Long>> tupleTrack = new ArrayList<ArrayList<Long>>();
    ArrayList<EventProxy> eventTrack = new ArrayList<EventProxy>();
    int autIndex = aps.indexOf(aut);
    StateProxy currState;
    StateProxy nextState;
    ArrayList<StateProxy> spsa = new ArrayList<StateProxy>();
    for (StateProxy s : sps) {
      spsa.add(s);
    }
    boolean hit = false;

    System.out.println("All Tuples: " + tuples);
    System.out.println("Tuples size: " + tuples.size() + " Target index: " + tuples.indexOf(target) + " " + (tuples.size() - 1));

    long index = 0;

    System.out.println("Target: " + target);

    while (curr != tuples.get(0)){
      //System.out.println("curr " + curr + " tuples 0 " + tuples.get(0));
      hit = false;
      //System.out.println("Tuples size: " + tuples.size() + " Target index: " + tuples.indexOf(target) + " " + (tuples.size() - 1));
      if(0 == tuples.size() -1){
        tupleTrack.add(curr);
        //target = curr;
        for (Long s : curr) {
          target.add(s);
        }
        curr = tuples.get(Math.toIntExact(index));
        break;
      }
      //System.out.println("Not Initial yet...");
      //while (index < tuples.size() - 1) {
      for (ArrayList<Long> tup : tuples){
        //System.out.println(curr);
        curr = tup;//tuples.get(Math.toIntExact(index));
        //System.out.println("new current, target");
        //System.out.println(curr);
        //System.out.println(target);
        // Mark this tuple as visited
        index++;

        for (EventProxy event : model.getEvents()) {
          EventKind ek = event.getKind();
          nexttup = new ArrayList<Long>();

          //System.out.println("Checking events...");

          if (ek == EventKind.UNCONTROLLABLE) {
            //System.out.println("valid event");
//            System.out.println(curr);
//            System.out.println(target);

            currState = sps[Math.toIntExact(curr.get(autIndex))];
            nextState = findSuccessorState(aut, currState, event);
            for (Long l : curr) {
              nexttup.add(l);
            }
            nexttup.set(autIndex, (long) spsa.indexOf(nextState));

            if (nexttup.equals(target)){
              //System.out.println("Target reached");
              tupleTrack.add(curr);
              eventTrack.add(event);
              //target = curr;
              hit = true;
              index = tuples.indexOf(target);
              break;
            }
            //else System.out.println("else? " + curr + " " + nexttup);
          }
          //ELSE event is controllable so not relevant to automata controllability according to controllability condition
        } if (hit) break;
        }
      //System.out.println("curr: " + curr + " initial: " + tuples.get(0));
      }

    System.out.println("Tuple Track; " + tupleTrack);
    System.out.println("event track: " + eventTrack);



    // The following creates a trace that consists of all the events in
    // the input model.
    // This code is only here to demonstrate the use of the interfaces.
    // IT DOES NOT GIVE A CORRECT COUNTEREXAMPLE!
    for (final EventProxy event : events) {
      eventList.add(event);
    }
    return
            desFactory.createSafetyCounterExampleProxy(traceName, model, eventList);
            // SHOULD RETURN when implementation works
            //   desFactory.createSafetyCounterExampleProxy(traceName, model, eventTrace);
  }


  //#########################################################################
  //# Data Members
  /**
   * The computed counterexample or null if the model is controllable.
   */
  private SafetyCounterExampleProxy mCounterExample;

}
