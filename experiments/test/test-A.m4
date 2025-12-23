define(`_FILE_', `test-A')
include(code.m4)

define(`_TIME_', `Discrete')

code(system,simple)

code(type,sig_union,t_if_signal,())

code(system_instance, simple)
code(end_system_instance)

code(instance,A,1,
	(),
	(x,clock),
	(),
	(top,integer),(top,-1,top,1,),
	())

code(dispatch,top_0,)

code(dispatch,start_1,top_0,
	(,1))

code(fire,start_1,1,EAGER,,,({0,0,0}),
	,((a)),b,,,5)
code(action,start_1,1,a,
	set,m_var.x,0)
code(action,start_1,1,b,
	nextstate,(top,-1,top,2,))


code(dispatch,first_2,top_0,
	(,1))

code(fire,first_2,1,EAGER,,,(constraint(m_var.x,0,2*((int) 10)+1),constraint(0,m_var.x,-2*(10)+1),{0,0,0}),
	,((a),(b)),c,,,9)
code(action,first_2,1,a,
	informal,"A")
code(action,first_2,1,b,
	set,m_var.x,0)
code(action,first_2,1,c,
	nextstate,(top,-1,top,1,))


code(instance_table,1,`
  {"top", 0, 1, 0 | CONTROL,
    {(char)0},
    &if_A_instance::_top_0_dispatch},
  {"start", 0, 1, 0 | TNONE,
    {(char)0},
    &if_A_instance::_start_1_dispatch},
  {"first", 0, 2, 0 | TNONE,
    {(char)0},
    &if_A_instance::_first_2_dispatch},
')

code(end_instance)


define(`x_use_priorities', `0')
code(start,(),(A),,time)code(priority_rule_begin, 1)
code(priority_rule_null, 0)