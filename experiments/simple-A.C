


/*
 *
 * IF-Toolset - Copyright (C) UGA - CNRS - G-INP
 * 
 * Auto-generated simulator code.
 * 
 */


#include <iostream>
#include "simulator.h"
#include "simple-A.h"
#include "filter.h"







inline void if_t_if_signal_copy
    (if_t_if_signal_type& x, const if_t_if_signal_type y) {
  x = y;
}
inline int if_t_if_signal_compare
    (const if_t_if_signal_type x, const if_t_if_signal_type y) {
  int c = x - y;
  if(c || x == NULL) return c;
  return x->compare(y);
}
inline void if_t_if_signal_print
    (const if_t_if_signal_type x, FILE* f) { 
  fprintf(f,"{");
  if(x) x->print(f);
  else fprintf(f,"nil");
  fprintf(f,"}");
}
inline void if_t_if_signal_print_xml
    (const if_t_if_signal_type x, std::ostream& b) { 
  b << "<sigunion>\n";
  if(x) x->printXML(b);
  b << "</sigunion>\n";
}
inline void if_t_if_signal_reset
    (if_t_if_signal_type& x) {
  x = NULL;
}



/* 
 * simple [system] instance implementation 
 *
 */

if_simple_instance::if_simple_instance(if_pid_type pid, IfQueue* queue) 
  : IfInstance(pid, queue) {
} 

if_simple_instance::if_simple_instance(const if_simple_instance& instance) 
  : IfInstance(instance) {
}




/* 
 * A instance implementation 
 *
 */





inline void if_A_var_copy
    (if_A_var_type& x, const if_A_var_type y) {
  if_clock_copy(x.x,y.x);
}
inline int if_A_var_compare
    (const if_A_var_type x, const if_A_var_type y) {
  int c = 0;
  if (c == 0) c = if_clock_compare(x.x,y.x);
  return c;
}
inline void if_A_var_print
    (const if_A_var_type x, FILE* f) { 
  fprintf(f,"{");
  fprintf(f,"x="); if_clock_print(x.x,f);
  fprintf(f,"}");
}
inline void if_A_var_print_xml
    (const if_A_var_type x, std::ostream& b) { 
  b << "<A_var>\n";
  b << "<x>"; if_clock_print_xml(x.x,b); b << "</x>"; 
  b << "</A_var>\n";
}
inline void if_A_var_reset
    (if_A_var_type& x) {
  if_clock_reset(x.x);
}

inline if_A_var_type if_A_var_make
    (if_clock_type x) {
  if_A_var_type m_par;
  if_clock_copy(m_par.x,x);
  return m_par;
}


inline void if_A_ctl_copy
    (if_A_ctl_type& x, const if_A_ctl_type y) {
  if_integer_copy(x.top,y.top);
}
inline int if_A_ctl_compare
    (const if_A_ctl_type x, const if_A_ctl_type y) {
  int c = 0;
  if (c == 0) c = if_integer_compare(x.top,y.top);
  return c;
}
inline void if_A_ctl_print
    (const if_A_ctl_type x, FILE* f) { 
  fprintf(f,"{");
  fprintf(f,"top="); if_integer_print(x.top,f);
  fprintf(f,"}");
}
inline void if_A_ctl_print_xml
    (const if_A_ctl_type x, std::ostream& b) { 
  b << "<A_ctl>\n";
  b << "<top>"; if_integer_print_xml(x.top,b); b << "</top>"; 
  b << "</A_ctl>\n";
}
inline void if_A_ctl_reset
    (if_A_ctl_type& x) {
  if_integer_reset(x.top);
}

inline if_A_ctl_type if_A_ctl_make
    (if_integer_type top) {
  if_A_ctl_type m_par;
  if_integer_copy(m_par.top,top);
  return m_par;
}

const char* if_A_instance::NAME = IfInstance::PROCNAME[1] = "A";



if_A_instance::if_A_instance() 
  : if_simple_instance(if_pid_mk(if_A_process,0), IfQueue::NIL) {
  m_ctl.top=-1;
  m_ctl.top=1;
  
    STATUS |=  CREATE;
  if_A_var_reset(m_var);
  if_A_par_reset(m_par);
    STATUS &= ~CREATE;
  
  
}

if_A_instance::if_A_instance(const if_A_instance& instance) 
  : if_simple_instance(instance) {
  if_A_ctl_copy(m_ctl,instance.m_ctl);
  if_A_var_copy(m_var,instance.m_var);
  if_A_par_copy(m_par,instance.m_par);
}

const char* if_A_instance::getState() const {
  static char r[1024];
  char tmp[256];
  *r = (char)0;
  if(m_ctl.top != -1) { sprintf(tmp,"top:%s,", STATE[m_ctl.top].name); strcat(r,tmp); }
  
  r[strlen(r)-1]=(char)0;
  return r;
}

int if_A_instance::is(const unsigned flag) const {
  int ok = 0;
  if (m_ctl.top != -1)
    ok |= (STATE[m_ctl.top].flags & flag);
  return ok;
}

int if_A_instance::compare(const IfInstance* X) const {
  if_A_instance* x = (if_A_instance*)X;
  int cmp = IfInstance::compare(X);
  if (cmp == 0) cmp = if_A_ctl_compare(m_ctl,x->m_ctl);
  if (cmp == 0) cmp = if_A_var_compare(m_var,x->m_var);
  if (cmp == 0) cmp = if_A_par_compare(m_par,x->m_par);
  return cmp;
}

unsigned long if_A_instance::hash(const unsigned long base) const {
  unsigned long key = m_pid;
  key += (unsigned long) m_queue; 
  if (sizeof(m_var) >= 4) // void 
    key += if_hash((char*) &m_var, sizeof(m_var), base);
  if (sizeof(m_par) >= 4) // void
    key += if_hash((char*) &m_par, sizeof(m_par), base);
  key += if_hash((char*) &m_ctl, sizeof(m_ctl), base);
  return key % base;
}

IfInstance* if_A_instance::copy() const {
  return new if_A_instance(*this);
}

void if_A_instance::print(FILE* f) const {
  // warning: print the topmost state name only
  IfInstance::print(f);
  fprintf(f, "\n  @%s\t\t", getState());
  if_A_var_print(m_var,f);
  fprintf(f, " ");
  if_A_par_print(m_par,f);
}

void if_A_instance::printXML(std::ostream& buf) const {
  buf << "<IfInstance ";
  buf << "type=\"A\" ";
  buf << "state=\"" << getState() << "\" >\n";    
  IfInstance::printXML(buf);

  buf << "<var>\n";     
  if_A_var_print_xml(m_var,buf);
  buf << "</var>\n";        

  buf << "<par>\n";     
  if_A_par_print_xml(m_par,buf);
  buf << "</par>\n";        
  buf << "</IfInstance>\n";   
}

void if_A_instance::copy(const IfInstance* X) {
  if_A_instance* x = (if_A_instance*) X;
  IfInstance::copy(X);
  if_A_ctl_copy(m_ctl,x->m_ctl);
  if_A_var_copy(m_var,x->m_var);
  if_A_par_copy(m_par,x->m_par);
}

void if_A_instance::reset() {
  if_A_ctl_reset(m_ctl);
  m_ctl.top=-1;
  m_ctl.top=1;
  
  if_A_var_reset(m_var);
  if_A_par_reset(m_par);
}

void if_A_instance::iterate(IfIterator* iterator) {
  STEP = 0;
  ITERATOR = iterator;
  fire();
  ITERATOR = NULL;
}


void if_A_instance::fire() {
  unsigned step = STEP;

// CS: no DISCARD since it complicates everything
//  if (opt_discard && (STATE[m_sp].flags & STABLE)) {
//    IfMessage* message = NULL;
//    for(IfQueue* queue = m_queue; 
//        queue != IfQueue::NIL; 
//        queue = (IfQueue*) queue->getTail()) {
//      message = (IfMessage*) queue->getHead();
//      if (!save(message->getSid()))
//        break;
//      message = NULL;
//    }
//    if (message != NULL) {
//      m_queue = m_queue->remove(message);
//      if (STATE[m_sp].flags & TSIG) {
//        ITERATOR->record();
//        (this->*(STATE[m_sp].dispatch))(message);
//        ITERATOR->forget();
//      }
//      if(STEP == step) {
//        IfTime::Constraint constraints[] = {{0,0,0}};
//        ITERATOR->guard(constraints,EAGER);      
//        ITERATOR->trace(IfEvent::DISCARD, m_pid, message->string(), m_pid, message->store());
//        nextstate(-1);
//      }
//    }
//    ITERATOR->restore();
//  } else {

  int unstable = is(UNSTABLE);

  if ( is(TSIG) ) {
    IfMessage* message = NULL;
    for(IfQueue* queue = m_queue; 
        queue != IfQueue::NIL; 
        queue = (IfQueue*) queue->getTail()) {
      message = (IfMessage*) queue->getHead();
      if (!save(message->getSid()))
        break;
      message = NULL;
    }
    if (message != NULL) {
      m_queue = m_queue->remove(message);
      ITERATOR->record();
      dispatch(message, unstable);
      ITERATOR->forget();
    }
    ITERATOR->restore();
  }
//  }
  if ( is(TNONE) )
    dispatch(NULL, unstable);
  if ( is(TSIG) ) {
    
  }

  if (is(UNSTABLE) && step == STEP)
      fprintf(stderr, "\nerror #2: unstable deadlock: %s@%s\n", 
          NAME, getState());
}

void if_A_instance::dispatch(IfMessage* message, int unstable) {
  if (m_ctl.top != -1 && (int)(STATE[m_ctl.top].flags & UNSTABLE) == unstable)
    (this->*(STATE[m_ctl.top].dispatch))(message);
  
}

void if_A_instance::nextstate() {

  if ( !is(UNSTABLE) )
    ITERATOR->step();
  else {
    ITERATOR->record();
    fire();
    ITERATOR->forget();
  }
}



void if_A_instance::_top_0_dispatch(IfMessage* message) {
  if (message != NULL)
    switch(message->getSid()) {
    }
  
}

void if_A_instance::_start_1_dispatch(IfMessage* message) {
  if (message != NULL)
    switch(message->getSid()) {
    }
  else {
    _start_1_1_fire(message);
      ITERATOR->restore();
      
    }
  _top_0_dispatch(message);
}

void if_A_instance::_start_1_1_fire(IfMessage* X) {
  IfTime::Constraint constraints[] = 
    {{0,0,0}};
  
  
  if (! ITERATOR->guard(constraints,EAGER))
    return; 
  if (!constraints[0].isEmpty() || EAGER!=EAGER) 
    if(getPriority() != OBSERVER_PRIORITY) ITERATOR->trace(IfEvent::DELTA, m_pid, "");
  
  if (opt_gen_lineno) ITERATOR->trace(IfEvent::DEBUG, m_pid, "5"); 
  
    _start_1_1a_fire();

  
  _start_1_1b_fire();
  
}
inline void if_A_instance::_start_1_1a_fire() {

  ITERATOR->set(&m_var.x,0,m_pid);
}
inline void if_A_instance::_start_1_1b_fire() {

  m_ctl.top=-1;
  m_ctl.top=2;
  
  nextstate();
}


void if_A_instance::_first_2_dispatch(IfMessage* message) {
  if (message != NULL)
    switch(message->getSid()) {
    }
  else {
    _first_2_1_fire(message);
      ITERATOR->restore();
      
    }
  _top_0_dispatch(message);
}

void if_A_instance::_first_2_1_fire(IfMessage* X) {
  IfTime::Constraint constraints[] = 
    {{m_var.x,0,2*((int) 10)+1},{0,m_var.x,-2*(10)+1},{0,0,0}};
  
  
  if (! ITERATOR->guard(constraints,EAGER))
    return; 
  if (!constraints[0].isEmpty() || EAGER!=EAGER) 
    if(getPriority() != OBSERVER_PRIORITY) ITERATOR->trace(IfEvent::DELTA, m_pid, "");
  
  if (opt_gen_lineno) ITERATOR->trace(IfEvent::DEBUG, m_pid, "9"); 
  
    _first_2_1a_fire();

  
  _first_2_1b_fire();
  
}
inline void if_A_instance::_first_2_1a_fire() {

  ITERATOR->set(&m_var.x,0,m_pid);
}
inline void if_A_instance::_first_2_1b_fire() {

  m_ctl.top=-1;
  m_ctl.top=3;
  
  nextstate();
}


void if_A_instance::_jitter_3_dispatch(IfMessage* message) {
  if (message != NULL)
    switch(message->getSid()) {
    }
  else {
    _jitter_3_1_fire(message);
      ITERATOR->restore();
      
    }
  _top_0_dispatch(message);
}

void if_A_instance::_jitter_3_1_fire(IfMessage* X) {
  IfTime::Constraint constraints[] = 
    {{m_var.x,0,2*((int) 1)+1},{0,0,0}};
  
  
  if (! ITERATOR->guard(constraints,DELAYABLE))
    return; 
  if (!constraints[0].isEmpty() || DELAYABLE!=EAGER) 
    if(getPriority() != OBSERVER_PRIORITY) ITERATOR->trace(IfEvent::DELTA, m_pid, "");
  
  if (opt_gen_lineno) ITERATOR->trace(IfEvent::DEBUG, m_pid, "15"); 
  
    _jitter_3_1a_fire();

  
  _jitter_3_1b_fire();
  
}
inline void if_A_instance::_jitter_3_1a_fire() {

  ITERATOR->trace(IfEvent::INFORMAL, m_pid, "A_START", m_pid, (IfObject*)"A_START");
}
inline void if_A_instance::_jitter_3_1b_fire() {

  m_ctl.top=-1;
  m_ctl.top=4;
  
  nextstate();
}


void if_A_instance::_wait_4_dispatch(IfMessage* message) {
  if (message != NULL)
    switch(message->getSid()) {
    }
  else {
    _wait_4_1_fire(message);
      ITERATOR->restore();
      
    }
  _top_0_dispatch(message);
}

void if_A_instance::_wait_4_1_fire(IfMessage* X) {
  IfTime::Constraint constraints[] = 
    {{m_var.x,0,2*((int) 5)+1},{0,m_var.x,-2*(5)+1},{0,0,0}};
  
  
  if (! ITERATOR->guard(constraints,EAGER))
    return; 
  if (!constraints[0].isEmpty() || EAGER!=EAGER) 
    if(getPriority() != OBSERVER_PRIORITY) ITERATOR->trace(IfEvent::DELTA, m_pid, "");
  
  if (opt_gen_lineno) ITERATOR->trace(IfEvent::DEBUG, m_pid, "20"); 
  
    _wait_4_1a_fire();

  
  _wait_4_1b_fire();
  
}
inline void if_A_instance::_wait_4_1a_fire() {

  ITERATOR->set(&m_var.x,0,m_pid);
}
inline void if_A_instance::_wait_4_1b_fire() {

  m_ctl.top=-1;
  m_ctl.top=3;
  
  nextstate();
}


if_state<1,if_A_instance::dispatcher> if_A_instance::STATE[] = {
  {"top", 0, 1, 0 | CONTROL,
    {(char)0},
    &if_A_instance::_top_0_dispatch},
  {"start", 0, 1, 0 | TNONE,
    {(char)0},
    &if_A_instance::_start_1_dispatch},
  {"first", 0, 2, 0 | TNONE,
    {(char)0},
    &if_A_instance::_first_2_dispatch},
  {"jitter", 0, 3, 0 | TNONE,
    {(char)0},
    &if_A_instance::_jitter_3_dispatch},
  {"wait", 0, 4, 0 | TNONE,
    {(char)0},
    &if_A_instance::_wait_4_dispatch},
};





IfConfig* IfIterator::start() {
  if_pid_type pid = 0;
  IfInstance* instance = NULL;
  IfConfig empty(1);
  
  m_config.put(&empty);

  
  instance = new if_A_instance;
  fork(instance, &pid);
  delete instance;
  

  

  instance = new IfDiscreteTime();
  fork(instance, &pid);
  delete instance; 

  // options
  opt_use_priorities = 0;

  return m_config.get();
}

IfPriorityFilter::Rule* IfPriorityFilter::RULE = new IfPriorityFilter::Rule[1];

IfPriorityFilter::Rule P0 = IfPriorityFilter::RULE[0] = NULL;
