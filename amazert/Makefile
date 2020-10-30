
BUILD_OUT_DIR ?= ./_out

default: all
all: package

dirs: 
	mkdir -p $(BUILD_OUT_DIR)/amazert

compile: dirs
	cp src/*.py $(BUILD_OUT_DIR)/amazert
	cp src/*.sh $(BUILD_OUT_DIR)/amazert
	cp src/amazert $(BUILD_OUT_DIR)/amazert


package: compile
	tar -czvf $(BUILD_OUT_DIR)/amazert.pkg -C $(BUILD_OUT_DIR) amazert

commit: $(BUILD_OUT_DIR)/amazert.pkg
	cp $(BUILD_OUT_DIR)/amazert.pkg ../aMAZE/app/src/main/assets/amazert.pkg
clean:
	rm -rf $(BUILD_OUT_DIR)
