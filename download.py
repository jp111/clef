import inspect
import sys
import os
import json
from os.path import isfile, join
from pubmed_lookup import PubMedLookup
from pubmed_lookup import Publication
from Bio import Entrez
from Bio import Medline
import time
try:
    from urllib.error import HTTPError  # for Python 3
except ImportError:
    from urllib2 import HTTPError  # for Python 2


# NCBI will contact user by email if excessive queries are detected
email = 'jaspreetsingh112@gmail.com'
base_url = 'http://www.ncbi.nlm.nih.gov/pubmed/'
Entrez.email = email
directory = '/home/devil/research/CLEF/ehealth/task2/dataset/Training Data/topics_train'
def download_articles():
	########################################################
	'''
	Make an articles object with the following format for
	each file present in "directory" and save as fiel.json
	in the same directory
	articles = {
	'Topic' = ""
	'Title' = ""
	'Query' = ""
	'Pids' = [
			{
			'LA', 'DP', 'CDAT', 'PL', 'AID', 'AU', 'AB',
			'CI', 'PMID', 'STAT', 'PT', 'PB', 'CTDT', 'DA',
			'DRDT', 'GR', 'BTI', 'FAU', 'FED', 'TI',
			'EDAT', 'ED', 'MHDA'
			}
			]
	}
	articles ['Pids'] is list of objects having the above keys
	where 'PMID' is the pubmed id 
	and 'AB' is the abstract
	'''
	########################################################
	for file in os.listdir(directory):
		if not isfile(join(directory,file)):
			continue
		filename = os.path.join(directory,file)
		target = open(filename,'r')

		articles = {}
		topic_line = target.readline()
		topic = topic_line.split(':')[1].strip()
		articles['Topic'] = topic

		target.readline()
		title_line = target.readline()
		title = title_line.split(':')[1].strip()
		articles['Title'] = title

		target.readline()
		target.readline()
		
		articles['Query'] = ""
		query = ""
		lines = list(target.readlines())
		for line in lines:
			if line == "\n":
				break
			else:
				query+=line
		articles['Query'] = query.strip()

		
		urls = []
		for line in lines:
			if "Pids" not in line and line!="\n":
				urls.append(line.strip())
		urls = urls[1:]

		#print("Total number of unique urls:" + str(len(urls)))
		handle = Entrez.efetch(db="pubmed", id=urls, rettype="medline",
                           retmode="text")
		records = Medline.parse(handle)	
		articles['pids'] = []

		records = list(records)
		articles['pids'] = records
		with open(os.path.join(directory,file)+'.json', 'w') as fp:
			json.dump(articles, fp)
		print("DONE " + file)
#		sys.exit()

def load_article(file):
	#######################
	'''
	load saved json articles
	'''
	#######################
	with open(os.path.join(directory,file)+'.json', 'r') as fp:
		articles = json.load(fp)
	#print (articles['pids'][0])
	"""
	for i in range(1, 10):
		try:
			print (i)
			print(
			
			TITLE:\n{title}\n
			AUTHORS:\n{authors}\n
			DATE PUBLISHED:\n{dp}
			ABSTRACT:\n{abstract}\n

			.format(**{
			    'title': articles['pids'][i]['TI'],
			    'dp': articles['pids'][i]['DP'],
			    'authors': articles['pids'][i]['AU'],
			    'abstract': articles['pids'][i]['AB'],
			}))
		except:
			pass
	"""

def print_article(file):
	print(articles['Title'],articles['pids'][0]['AB'],articles['pids'][0].keys())


def divide_into_directory(file):
	"""
	divide "file" number file's json data into separate files and save in a directory
	to be indexed by lucene
	"""
	with open(os.path.join(directory,file)+'.json', 'r') as fp:
		articles = json.load(fp)
	
	articles_directory = os.path.join(directory,file)+'_articles'
	if not os.path.exists(articles_directory):
		os.mkdir(articles_directory)
	
	

	#print (len(articles['pids']))
	for i in range (1,len(articles['pids'])):
		if i%30 == 0:
			print (i)
		individual_file = os.path.join(articles_directory,articles['pids'][i]['PMID'])
		with open(individual_file, 'w+') as fp:
			#print(articles['pids'][i])
			try:
				fp.write(articles['pids'][i]['TI'])
				fp.write(articles['pids'][i]['AB'])
			except Exception as e:
				print(record['PMID'])
			"""
			for value in articles['pids'][i]:
				#print(value)
				#print(articles['pids'][i][value])
				try:
					fp.write(articles['pids'][i][value])
				except:
					for values in value:
						fp.write(values)
						fp.write(' ')
				fp.write('\n')
				#fp.write('\n')
			"""

"""
query only fetches first 10000 results
"""
def remaining(file):
	urls=[]
	handle = Entrez.efetch(db="pubmed", id=urls, rettype="medline",
                           retmode="text")
	records = Medline.parse(handle)	
	articles_directory = os.path.join(directory,file)+'_articles'
	if not os.path.exists(articles_directory):
		os.mkdir(articles_directory)
	i = 0
	for record in records:
		if i%20 == 0:
			print (i)
		i=i+1
		individual_file = os.path.join(articles_directory,record['PMID'])
		
		with open(individual_file, 'w+') as fp:
			#print(articles['pids'][i])
			try:
				fp.write(record['TI'])
				fp.write(record['AB'])
			except Exception as e:
				print(record['PMID'])


