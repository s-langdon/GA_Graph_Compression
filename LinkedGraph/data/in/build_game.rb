SOURCES = {
  fallout_4: 'fallout4.txt',
  fallout_nv: 'falloutNV.txt',
  skyrim: 'Skyrim.txt',
  oblivion: 'Oblivion.txt'
}
OUTPUT_EXTENSION = 'dat'
CONSTANT_PARAMETERS = {
  generations: 500,
  population: 100,
  tournament: 5,
  runs: 10,
  elites: 1,
  mutation: 0.10,
  crossover: 0.90,
}
PARAMETERS = [
  {
    compression: 0.10,
    'maxDistance': 3,
  },{
    compression: 0.25,
    'maxDistance': 3,
  },{
    compression: 0.10,
    'maxDistance': 3,
  },{
    compression: 0.10,
    'maxDistance': 5,
  },{
    compression: 0.25,
    'maxDistance': 5,
  },{
    compression: 0.40,
    'maxDistance': 5,
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
