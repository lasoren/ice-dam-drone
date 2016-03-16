CPP=g++
LIBS=`pkg-config --cflags --libs opencv`

match_icicles: match_icicles.cpp
	$(CPP) $^ $(LIBS) -o $@

clean:
	rm -rf match_icicles
