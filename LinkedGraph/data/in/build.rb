SOURCES = {
  ecoli: 'ecoli.txt', 
  yeast: 'yeast.txt', 
  figeys: 'figeys.txt'
}
OUTPUT_EXTENSION = 'dat'
CONSTANT_PARAMETERS = {
  generations: 500,
  population: 100,
  tournament: 5,
  runs: 5,
  elites: 1
}
PARAMETERS = [
  {
    compression: 0.25,
    mutation: 0.10,
    crossover: 0.90,
    'maxDistance': 10
  },{
    compression: 0.10,
    mutation: 0.10,
    crossover: 0.90,
    'maxDistance': 5
  },{
    compression: 0.20,
    mutation: 0.10,
    crossover: 0.90,
    'maxDistance': 5
  },{
    compression: 0.25,
    mutation: 0.10,
    crossover: 0.90,
    'maxDistance': 5
  },{
    compression: 0.25,
    mutation: 0.10,
    crossover: 0.90,
    'maxDistance': 3
  },{
    compression: 0.10,
    mutation: 0.10,
    crossover: 0.90,
    'maxDistance': 3
  },{
    compression: 0.20,
    mutation: 0.10,
    crossover: 0.90,
    'maxDistance': 3
  },
]
SOURCES.each do |prefix, source_file|
  PARAMETERS.each_with_index do |params, index|
    parameter_set_file = File.new("#{prefix}#{index+1}.#{OUTPUT_EXTENSION}", "w")

    parameter_set_file.puts("outPrefix #{prefix}")
    parameter_set_file.puts("source #{source_file}")
    CONSTANT_PARAMETERS.each do |key, value|
      parameter_set_file.puts("#{key} #{value}")
    end
    params.each do |key, value|
      parameter_set_file.puts("#{key} #{value}")
    end

    parameter_set_file.close
  end
end
