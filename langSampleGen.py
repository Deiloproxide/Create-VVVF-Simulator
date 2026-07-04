import json
mod_id:str='create_vvvf_simulator'
tooltip:str='.tooltip'
length:int=6
prefixes:list[str]=['.configuration.','.train.dimension.','.train.event.',
                    '.command.load.status.','.command.load.exception.','.command.reload.']
keyss:list[list[str]]=[['network','sync_period','environment','eval_period',
                       'audio','sample_rate','buffer_size','conv_size','table_size','tail_size',
                       'speed_smoother','speeds_length','max_acc_ratio',
                       'audio_volumes','main_amp','gas_amp','switch_amp',
                       'base_amp','brown_amp','vvvf_amp','bg_wind_amp','main_wind_amp',
                       'spread_distance','near_distance','far_distance',
                       'advanced','equipment_sound','brown_sigma','brown_range','base_current_f',
                       'base_harmonic','first_amp','second_amp','third_amp','fourth_amp',
                       'first_phase','second_phase','third_phase','fourth_phase',
                       'vvvf_sound','first_gear','second_gear','max_speed_f',
                       'motor_db','gear_harmonic_db','dry_wet_ratio','line_train_ratio',
                       'bg_wind_sound','pink_r0','wind_base_amp','wind_mod_f','wind_mod_depth',
                       'bg_shear_base','bg_shear_range','bg_shear_rate','hp_cutoff',
                       'main_wind_sound','main_cauchy_amp','main_center_f',
                       'main_cauchy_gamma','main_mod_f','main_mod_depth'],
                       ['overworld','nether','end'],
                       ['crash','stress','portal','end','miss'],
                       ['ok','fallback','error'],
                       ['normal','invalid','notfound','io','empty',
                        'lex','parse','compose','dump','init'],
                       ['ok']]
sample:dict[str,str]={}
for i in range(length):
    prefix:str=prefixes[i]
    keys:list[str]=keyss[i]
    for key in keys: sample[mod_id+prefix+key]=''
    if i==0:
        for key in keys: sample[mod_id+prefix+key+tooltip]=''
file=open('sample.json','w',encoding='utf-8')
json.dump(sample,file,indent=4)
file.close()