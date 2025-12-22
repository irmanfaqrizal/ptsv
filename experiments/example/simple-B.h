


/*
 *
 * IF-Toolset - Copyright (C) UGA - CNRS - G-INP
 * 
 * Auto-generated simulator code.
 * 
 */









typedef IfMessage* if_t_if_signal_type;

#define if_t_if_signal_eq(x,y) if_t_if_signal_compare(x,y)==0
#define if_t_if_signal_ne(x,y) if_t_if_signal_compare(x,y)!=0


/* 
 * simple [system] instance interface 
 *
 */

class if_simple_instance : public IfInstance { 

  public:
    if_simple_instance(if_pid_type = 0, IfQueue* = IfQueue::NIL);
    if_simple_instance(const if_simple_instance&);

  protected:

};


/* 
 * B instance interface 
 *
 */
 
#define if_B_process 1



typedef if_void_type if_B_par_type;

#define if_B_par_copy(x,y) if_void_copy(x,y)
#define if_B_par_compare(x,y) if_void_compare(x,y)
#define if_B_par_print(x,f) if_void_print(x,f)
#define if_B_par_print_xml(x,b) if_void_print_xml(x,b)
#define if_B_par_reset(x) if_void_reset(x)
#define if_B_par_iterate(x) if_void_iterate(x)
#define if_B_par_eq(x,y) if_B_par_compare(x,y)==0
#define if_B_par_ne(x,y) if_B_par_compare(x,y)!=0


typedef struct {
  if_clock_type x;
} if_B_var_type;

inline void if_B_var_copy
    (if_B_var_type& x, const if_B_var_type y);
inline int if_B_var_compare
    (const if_B_var_type x, const if_B_var_type y);
inline void if_B_var_print
    (const if_B_var_type x, FILE* f);
inline void if_B_var_reset
    (if_B_var_type& x);
#define if_B_var_iterate(_x_)\
  if_clock_iterate(_x_.x)
inline if_B_var_type if_B_var_make
    (if_clock_type x);
#define if_B_var_eq(x,y) if_B_var_compare(x,y)==0
#define if_B_var_ne(x,y) if_B_var_compare(x,y)!=0


typedef struct {
  if_integer_type top;
} if_B_ctl_type;

inline void if_B_ctl_copy
    (if_B_ctl_type& x, const if_B_ctl_type y);
inline int if_B_ctl_compare
    (const if_B_ctl_type x, const if_B_ctl_type y);
inline void if_B_ctl_print
    (const if_B_ctl_type x, FILE* f);
inline void if_B_ctl_reset
    (if_B_ctl_type& x);
#define if_B_ctl_iterate(_x_)\
  if_integer_iterate(_x_.top)
inline if_B_ctl_type if_B_ctl_make
    (if_integer_type top);
#define if_B_ctl_eq(x,y) if_B_ctl_compare(x,y)==0
#define if_B_ctl_ne(x,y) if_B_ctl_compare(x,y)!=0

class if_B_instance : public if_simple_instance {

public:
  
  if_B_instance();
  if_B_instance(const if_B_instance&);

  inline int GetSP() const { return m_ctl.top; }	// still used by observer operator "instate" -> only working without concurrent SMs

public:
  virtual const char* getState() const;
  virtual int is(const unsigned) const;

public:
  virtual int compare(const IfInstance*) const;
  virtual unsigned long hash(const unsigned long) const;
  virtual IfInstance* copy() const;
  virtual void print(FILE*) const;
  virtual void printXML(std::ostream&) const;

public: 
  virtual void reset();
  virtual void iterate(IfIterator*);
  virtual void copy(const IfInstance*);

public:
  static const char* NAME;

public:
  
  inline if_clock_type& x()
    { return m_var.x; }
  

private: 
  if_B_par_type m_par;  /* parameters */ 
  if_B_var_type m_var;  /* variables */
  if_B_ctl_type m_ctl;  /* control */

private:  
  inline int input(unsigned signal) const {
    int ok = 0;
    if (m_ctl.top != -1)
      ok |= (STATE[m_ctl.top].sigtab[signal] & 1);
    return ok;
  }
  inline int save(unsigned signal) const {
    int ok = 0;
    if (m_ctl.top != -1) 
      ok |= (STATE[m_ctl.top].sigtab[signal] & 2);
    return ok;
  }

private:
         void fire();
//         void tpc();
         void dispatch(IfMessage*, int);
  inline void nextstate();

         void _top_0_dispatch(IfMessage*);

         void _start_1_dispatch(IfMessage*);

         void _start_1_1_fire(IfMessage*);
  inline void _start_1_1a_fire();
  inline void _start_1_1b_fire();


         void _first_2_dispatch(IfMessage*);

         void _first_2_1_fire(IfMessage*);
  inline void _first_2_1a_fire();
  inline void _first_2_1b_fire();


         void _jitter_3_dispatch(IfMessage*);

         void _jitter_3_1_fire(IfMessage*);
  inline void _jitter_3_1a_fire();
  inline void _jitter_3_1b_fire();


         void _wait_4_dispatch(IfMessage*);

         void _wait_4_1_fire(IfMessage*);
  inline void _wait_4_1a_fire();
  inline void _wait_4_1b_fire();


private:
  typedef void (if_B_instance::*dispatcher)(IfMessage*);
//  typedef void (if_B_instance::*tpcchecker)();
  static if_state<1,dispatcher/*,tpcchecker*/> STATE[];


};




