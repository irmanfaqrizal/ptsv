The syntax described below is a subset of the [IF language](https://www-verimag.imag.fr/~async/IF/tutorials.html) that we use to specify our DSTA model.

<pre>
<i>system-decl</i> ::= <i>system-default</i> | <i>system-minimized</i>

<i>system-default</i> ::= 
    <b>system</b> system-id<b>;</b>
        {<i>process-decl</i>}*
    <b>endsystem;</b>

<i>system-minimized</i> ::= 
    <b>system<b> system-id<b>;</b> <b>[strong]</b>
        {<i>process-decl</i>}*
    <b>endsystem;</b>

<i>process-decl</i> ::=
    <b>process</b> process-id <b>(1);</b>
        {<i>process-component</i>}*
    <b>endprocess;</b>

<i>process-component</i> ::= <i>clock-decl</i> | <i>state-decl</i>

<i>clock-decl</i> ::= <b>var</b> clock-id <b>clock;</b>

<i>state-decl</i> ::=
    <b>state</b> state-id {<b>#start</b>};
        <i>transition</i> | <i>transition-with-delay1</i> | <i>transition-with-delay2</i>
    endstate

<i>transition</i> ::= 
    <b>when</b> clock-id <b>=</b> int<b>;</b>
        {<i>action</i> | <i>reset</i>}
        <b>nextstate</b> state-id<b>;</b>

<i>transition-with-delay1</i> ::= 
    <b>deadline</b> <b>delayable;</b>
    <b>when</b> clock-id <b><=</b> int;
        {<i>action-with-delay</i> | <i>reset</i>}
        <b>nextstate</b> state-id;

<i>transition-with-delay2</i> ::= 
    <b>deadline</b> <b>delayable;</b>
    <b>when</b> clock-id <b>>=</b> int <b>and</b> clock-id <b><=</b> int<b>;</b>
        {<i>action-with-delay</i> | <i>reset</i>}
        <b>nextstate</b> state-id;

<i>action</i> ::= <b>informal</b> "action-id"<b>;</b>

<i>action-with-delay</i> ::= <b>informal</b> "action-id"<b>;</b> [<i>dist</i>]

<i>reset</i> ::= <b>set</b> clock-id <b>:=</b> 0<b>;</b>

<i>dist</i> ::= <b>uniform</b> | <b>custom:</b><i>F</i>

<i>F</i> = f0, f1, f2, ..., fn, where each fi is a fractional number, and n is the length of the associated delays.

</pre

    