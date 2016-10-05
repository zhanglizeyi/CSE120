JAVADOCPARAMS = -doctitle "Nachos 5.0 Java" -protected \
		-link http://java.sun.com/j2se/1.5.0/docs/api/

machine =	Lib Config Stats Machine TCB \
		Interrupt Timer \
		Processor TranslationEntry \
		SerialConsole StandardConsole \
		OpenFile OpenFileWithPosition ArrayFile FileSystem StubFileSystem \
		ElevatorBank ElevatorTest ElevatorGui \
		ElevatorControls ElevatorEvent ElevatorControllerInterface \
		RiderControls RiderEvent RiderInterface \
		Kernel Coff CoffSection \
		NetworkLink Packet MalformedPacketException

security =	Privilege NachosSecurityManager

ag =		AutoGrader BoatGrader

threads =	ThreadedKernel KThread Alarm \
		Scheduler ThreadQueue RoundRobinScheduler \
		Semaphore Lock Condition SynchList \
		Condition2 Communicator Rider ElevatorController \
		PriorityScheduler LotteryScheduler Boat

userprog =	UserKernel UThread UserProcess SynchConsole

vm =		VMKernel VMProcess

network = 	NetKernel NetProcess PostOffice MailMessage

ALLDIRS = machine security ag threads userprog vm network

PACKAGES := $(patsubst %,nachos.%,$(ALLDIRS))

CLASSFILES := $(foreach dir,$(DIRS),$(patsubst %,nachos/$(dir)/%.class,$($(dir))))

.PHONY: all rmtemp clean doc hwdoc swdoc

all: $(CLASSFILES)

nachos/%.class: ../%.java
	javac -classpath . -d . -sourcepath ../.. -g $<

clean:
	rm -f */*/*.class

doc:
	mkdir -p ../doc
	javadoc $(JAVADOCPARAMS) -d ../doc -sourcepath .. $(PACKAGES)

test:
	cd ../test ; gmake

ag:	$(patsubst ../ag/%.java,nachos/ag/%.class,$(wildcard ../ag/*.java))
