import json
prefix:str='create_vvvf_simulator.configuration.'
tooltip:str='.tooltip'
json_dic:dict[str,str]={}
fields:list[str]=['network','sync_period','environment','eval_period',
                  'audio','sample_rate','buffer_size','conv_size','table_size','tail_size',
                  'speed_smoother','speeds_length','max_acc_ratio',
                  'audio_volumes','main_amp','gas_amp','switch_amp',
                  'base_amp','brown_amp','vvvf_amp','bg_wind_amp','main_wind_amp',
                  'spread_distance','near_distance','far_distance',
                  'advanced','equipment_sound','brown_sigma','brown_range','base_current_f',
                  'base_harmonic','first_amp','second_amp','third_amp','fourth_amp',
                  'first_phase','second_phase','third_phase','fourth_phase',
                  'vvvf_sound','dry_wet_ratio','line_train_ratio',
                  'bg_wind_sound','pink_r0','wind_base_amp','wind_mod_f','wind_mod_depth',
                  'bg_shear_base','bg_shear_range','bg_shear_rate','hp_cutoff',
                  'main_wind_sound','main_cauchy_amp','main_center_f',
                  'main_cauchy_gamma','main_mod_f','main_mod_depth']
for field in fields:
    json_dic[prefix+field]=''
    json_dic[prefix+field+tooltip]=''
fl=open('sample.json','w',encoding='utf-8')
json.dump(json_dic,fl,indent=4)
fl.close()