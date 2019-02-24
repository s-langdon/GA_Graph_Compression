p "Beginning to process output files"

require 'csv'

p "Processing output files"

files = Dir.entries('.')
VALID_OUTPUT_FILES = ["figey", "ecoli", "yeast"]
RUNS=5
GENERATIONS=1000

files.each_with_index do |filename, index|
  next unless VALID_OUTPUT_FILES.include? filename[0..4]
  output_data = CSV.table(filename)

  runs = []
  (1..RUNS).each do |i|
    runs << output_data[GENERATIONS * i - 1]
  end

  p "#########################"
  p "Report #{index + 1}/#{files.count}"
  p "File: #{filename}"
  p "Source: #{runs[0][:source]}"
  p "Graph Size: #{runs[0][:graph_size]}"
  p "Compression Rate: #{runs[0][:compression_rate]}"
  p "Elistism Rate: #{runs[0][:elitism_rate]}"
  p "Tournament Size: #{runs[0][:tournament_size]}"
  p "Mutation Rate: #{runs[0][:mutation_rate]}"
  p "Crossover Rate: #{runs[0][:crossover_rate]}"
  p "Maximum Neighbor Distance: #{runs[0][:maximum_distance]}"

  global_best = runs.last[:global_best_fitness]

  total = 0
  runs.each_with_index do |data, index|
    total += data[:run_best_fitness]
    p "Run #{index + 1} Best Fitness: #{data[:run_best_fitness]}"
  end
  run_best_average = total / runs.length

  p "#########"
  p "Global Best Fitness: #{global_best}"
  p "Best Fitness Average: #{run_best_average}"
end

p "Completed processing output files"
