Just something to tide over until somebody comes along and does something better. I encourage you to steal this and make something better <3

I've only played a bit of create so far, but paired with the ReactiveStress mod, it really made me yearn to have flywheels DO something. 

I'm not sure where the best balance between flywheel realism, create stress logic, and fun, so most of the variables are exposed in the config. 
lmk if something isnt working right or if a different mechanic would be more fun! 

Compatibility:
Full compatability with "Create Reactive Stress mod", this is an unofficial compansion mod 
integration with "Create: The Factory Must Grow"'s added flywheels. Each one has a unique niche.  

Incompatibility:
Pretty serious mixins for the create mod basic stress network, anything that modifies that might collide, but it seems to work fine in my 600+ mod pack
Has not been tested whatsoever with create aeronautics or other sable stuff. my computer isnt cool enough for that.  

TODO someday: 
    hurt helmates more 
    optimize create networ update calls
    option to toggle between flywheels are batteries vs flywheels spin up and down togather 
    option to toggle nearest flywheel to the overstress point gets consumed first 
    balance values more carefully 
    improve physics calculations
    translation strings


gritty nitty implementation stuff: 

    create mod doesnt really support the idea of batteries and energy packets like RF or something, so instead I injected a new type for the create network to track, capable of promising energy to the network when the network becomes overstressed, which is then subtracted from the stress in the network to bring it back down below threshold (until internal stores run out).

    its not the MOST performant thing but it seems mostly fine. The problem is mainly the create network rediscovering every networked machine on every network update, which may or may not be fixable via mixins idk. Might have a go at that later. Copycat flywheels might also be fun :)










No seriously this is an alpha release its just done enough that it works on my personal server so I wanna get it out there. 