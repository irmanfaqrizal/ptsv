define(`_FILE_', `simpler')
include(code.m4)

define(`_TIME_', `Discrete')

code(system,simpler)

code(type,sig_union,t_if_signal,())

code(system_instance, simpler)
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


code(dispatch,wait_2,top_0,
	(,1))

code(fire,wait_2,1,EAGER,,,(constraint(m_var.x,0,2*((int) 1)+1),constraint(0,m_var.x,-2*(1)+1),{0,0,0}),
	,((a)),b,,,9)
code(action,wait_2,1,a,
	set,m_var.x,0)
code(action,wait_2,1,b,
	nextstate,(top,-1,top,3,))


code(dispatch,first_3,top_0,
	(,1))

code(fire,first_3,1,DELAYABLE,,,(constraint(m_var.x,0,2*((int) 1)+1),{0,0,0}),
	,((a)),b,,,15)
code(action,first_3,1,a,
	informal,"A")
code(action,first_3,1,b,
	nextstate,(top,-1,top,1,))


code(instance_table,1,`
  {"top", 0, 1, 0 | CONTROL,
    {(char)0},
    &if_A_instance::_top_0_dispatch},
  {"start", 0, 1, 0 | TNONE,
    {(char)0},
    &if_A_instance::_start_1_dispatch},
  {"wait", 0, 2, 0 | TNONE,
    {(char)0},
    &if_A_instance::_wait_2_dispatch},
  {"first", 0, 3, 0 | TNONE,
    {(char)0},
    &if_A_instance::_first_3_dispatch},
')

code(end_instance)


code(instance,B,2,
	(),
	(x,clock),
	(),
	(top,integer),(top,-1,top,1,),
	())

code(dispatch,top_0,)

code(dispatch,start_1,top_0,
	(,1))

code(fire,start_1,1,EAGER,,,({0,0,0}),
	,((a)),b,,,23)
code(action,start_1,1,a,
	set,m_var.x,0)
code(action,start_1,1,b,
	nextstate,(top,-1,top,2,))


code(dispatch,wait_2,top_0,
	(,1))

code(fire,wait_2,1,EAGER,,,(constraint(m_var.x,0,2*((int) 3)+1),constraint(0,m_var.x,-2*(3)+1),{0,0,0}),
	,(),a,,,27)

code(action,wait_2,1,a,
	nextstate,(top,-1,top,3,))


code(dispatch,first_3,top_0,
	(,1))

code(fire,first_3,1,EAGER,,,({0,0,0}),
	,((a)),b,,,31)
code(action,first_3,1,a,
	informal,"B")
code(action,first_3,1,b,
	nextstate,(top,-1,top,1,))


code(instance_table,1,`
  {"top", 0, 1, 0 | CONTROL,
    {(char)0},
    &if_B_instance::_top_0_dispatch},
  {"start", 0, 1, 0 | TNONE,
    {(char)0},
    &if_B_instance::_start_1_dispatch},
  {"wait", 0, 2, 0 | TNONE,
    {(char)0},
    &if_B_instance::_wait_2_dispatch},
  {"first", 0, 3, 0 | TNONE,
    {(char)0},
    &if_B_instance::_first_3_dispatch},
')

code(end_instance)


define(`x_use_priorities', `0')
code(start,(),(A,B),,time)code(priority_rule_begin, 1)
code(priority_rule_null, 0)