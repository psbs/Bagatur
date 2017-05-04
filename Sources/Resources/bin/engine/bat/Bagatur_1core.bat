

REM Goes to parent directory (the root of the distribution)
cd ..

echo off

REM Sets path to binaries
set BIN_PATH=./bin/


REM Sets the path to jar files containing the compiled java code of Bagatur engine
set JARS_PATH=%BIN_PATH%egtbprobe.jar;%BIN_PATH%BagaturBoard.jar;%BIN_PATH%BagaturOpening.jar;%BIN_PATH%BagaturSearch.jar;%BIN_PATH%BagaturUCI.jar;%BIN_PATH%BagaturEngines.jar;%BIN_PATH%BagaturLearningAPI.jar;%BIN_PATH%BagaturEGTB.jar;%BIN_PATH%BagaturLearningImpl.jar;


REM Sets the memory (in megabytes) which the WHOLE java process will use.
REM One significant part of this memory (up to 35%) will be used for Transposition Table.
set PROCESS_MEMORY=1024M

set ARGS=bagaturchess.engines.base.cfg.UCIConfig_BaseImpl
set ARGS=%ARGS% bagaturchess.search.impl.uci_adaptor.UCISearchAdaptorImpl_PonderingOpponentMove
set ARGS=%ARGS% bagaturchess.engines.base.cfg.UCISearchAdaptorConfig_BaseImpl
set ARGS=%ARGS% bagaturchess.search.impl.rootsearch.sequential.SequentialSearch_MTD
REM set ARGS=%ARGS% bagaturchess.search.impl.rootsearch.parallel.MTDParallelSearch
set ARGS=%ARGS% bagaturchess.engines.base.cfg.RootSearchConfig_BaseImpl_1Core
REM set ARGS=%ARGS% bagaturchess.engines.base.cfg.RootSearchConfig_BaseImpl_SMP
set ARGS=%ARGS% bagaturchess.search.impl.alg.impl1.Search_NegaScout
REM set ARGS=%ARGS% bagaturchess.search.impl.alg.impl0.Search_PVS_NWS
set ARGS=%ARGS% bagaturchess.engines.bagatur.cfg.search.SearchConfigImpl_MTD_SMP
set ARGS=%ARGS% bagaturchess.learning.goldmiddle.impl.cfg.bagatur_allfeatures.filler.Bagatur_ALL_BoardConfigImpl
set ARGS=%ARGS% bagaturchess.learning.goldmiddle.impl.cfg.bagatur_allfeatures.eval.EvaluationConfig

echo on

REM Executes the java process of the Bagatur engine with sequential (not parallel) search. It uses only one CPU Core.
java.exe -Xmx%PROCESS_MEMORY% -Djava.library.path=%BIN_PATH% -cp %JARS_PATH% bagaturchess.uci.run.Boot %ARGS%

