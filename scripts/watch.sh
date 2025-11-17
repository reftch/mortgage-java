#!/bin/bash

# Directories to watch (defaults to current directory if not provided)
DIRS_TO_WATCH=()
if [ $# -eq 0 ]; then
    # Default to current directory if no arguments provided
    DIRS_TO_WATCH=(.)
else
    # Use all provided arguments as directories to watch
    for dir in "$@"; do
        if [ -d "$dir" ]; then
            DIRS_TO_WATCH+=("$dir")
        else
            echo "Warning: Directory '$dir' does not exist, skipping..."
        fi
    done
fi

# Command to start your Java application
JAVA_CMD="./gradlew run --no-problems-report"  # Change to your run command or java -jar

# PID of the running Java process
JAVA_PID=0

# Store PIDs of fswatch processes for cleanup
FSWATCH_PIDS=()

start_java() {
  # echo "Starting Java application..."
  $JAVA_CMD &
  JAVA_PID=$!
  # echo "Java started with PID $JAVA_PID"
}

stop_java() {
  if [ $JAVA_PID -ne 0 ]; then
    # echo "Stopping Java application with PID $JAVA_PID"
    kill $JAVA_PID 2>/dev/null
    wait $JAVA_PID 2>/dev/null
    JAVA_PID=0
  fi
}

# Function to kill processes on port 8080
kill_process_on_port() {
  # echo "Killing processes on port 8080..."
  # Find processes occupying port 8080 and kill them
  lsof -t -i tcp:8080 | xargs -r kill -9 2>/dev/null
}

cleanup() {
  # echo "Ctrl-C pressed: Cleaning up..."
  stop_java
  kill_process_on_port

  # Kill all Gradle-related processes that may be left running
  # echo "Stopping all Gradle processes..."
  ./gradlew --stop
  # pkill -f gradle 2>/dev/null

  # Kill all fswatch processes
  for fswatch_pid in "${FSWATCH_PIDS[@]}"; do
    if [ -n "$fswatch_pid" ] && kill -0 "$fswatch_pid" 2>/dev/null; then
      # echo "Killing fswatch process $fswatch_pid"
      kill "$fswatch_pid" 2>/dev/null
    fi
  done

  exit 0
}

# Setup trap to catch Ctrl-C (SIGINT) and call cleanup
trap cleanup SIGINT

# Function to start watching a single directory
start_watching_dir() {
  local dir="$1"
  # echo "Starting to watch directory: $dir"
  
  # Start fswatch for this directory in background
  fswatch -o --event Created --event Updated --event Removed -r "$dir" 2>/dev/null | while read num; do
    echo "File change detected in $dir, restarting Java application..."
    stop_java
    start_java
  done &
  
  # Store the PID of this fswatch process
  local fswatch_pid=$!
  FSWATCH_PIDS+=("$fswatch_pid")
  
  # echo "fswatch for $dir started with PID $fswatch_pid"
}

# Function to start watching all directories
start_watching_all() {
  # echo "Starting to watch ${#DIRS_TO_WATCH[@]} directories:"
  # for dir in "${DIRS_TO_WATCH[@]}"; do
  #   echo "  - $dir"
  # done
  
  # Start watching each directory
  for dir in "${DIRS_TO_WATCH[@]}"; do
    start_watching_dir "$dir"
  done
}

# Start Java initially
start_java

# Start watching all directories
start_watching_all

# Wait for all fswatch processes to complete
wait

# This line should never be reached due to the trap, but just in case:
echo "All watching processes have terminated"