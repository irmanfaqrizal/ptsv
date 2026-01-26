define(`_FILE_', `nondeterministic-Test')
include(code.m4)

define(`_TIME_', `Discrete')

code(system,nondeterministic)

code(type,sig_union,t_if_signal,())

code(system_instance, nondeterministic)
code(end_system_instance)

code(instance,Test,1,
	(),
	(x,clock),
	(),
	(top,integer),(top,-1,top,1,),
	())

code(dispatch,top_0,)

code(dispatch,start_1,top_0,
	(,1))

code(fire,start_1,1,EAGER,,,({0,0,0}),
	,((a)),b,,,6)
code(action,start_1,1,a,
	set,m_var.x,0)
code(action,start_1,1,b,
	nextstate,(top,-1,top,2,))


code(dispatch,first_2,top_0,
	(,1))

code(fire,first_2,1,EAGER,,,({0,0,0}),
	,(),a,,,10)

code(action,first_2,1,a,
	nextstate,(top,-1,top,3,))


code(dispatch,second_3,top_0,
	(,1))

code(fire,second_3,1,DELAYABLE,,,(constraint(m_var.x,0,2*((int) 2)+1),{0,0,0}),
	,((a),(b)),c,,,14)
code(action,second_3,1,a,
	set,m_var.x,0)
code(action,second_3,1,b,
	informal,"two")
code(action,second_3,1,c,
	nextstate,(top,-1,top,2,))


code(instance_table,1,`
  {"top", 0, 1, 0 | CONTROL,
    {(char)0},
    &if_Test_instance::_top_0_dispatch},
  {"start", 0, 1, 0 | TNONE,
    {(char)0},
    &if_Test_instance::_start_1_dispatch},
  {"first", 0, 2, 0 | TNONE,
    {(char)0},
    &if_Test_instance::_first_2_dispatch},
  {"second", 0, 3, 0 | TNONE,
    {(char)0},
    &if_Test_instance::_second_3_dispatch},
')

code(end_instance)


define(`x_use_priorities', `0')
code(start,(),(Test),,time)code(priority_rule_begin, 1)
code(priority_rule_null, 0)